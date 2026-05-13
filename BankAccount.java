import java.sql.*;
import java.util.ArrayList;

public class BankAccount {

    private String card;
    private int pin;
    private double balance;
    private ArrayList<String> history;

    public BankAccount(String card, int pin, double balance) {
        this.card = card;
        this.pin = pin;
        this.balance = balance;
        history = new ArrayList<>();
    }

    public String getCard() { return card; }
    public int getPin() { return pin; }

    // 🔥 GET BALANCE (Database + History Fallback)
    public double getBalance() {
        double bal = 0;

        try {
            Connection con = DBConnection.getConnection();

            if(con != null) {
                PreparedStatement ps = con.prepareStatement(
                    "SELECT balance FROM users WHERE card_number=?"
                );

                ps.setString(1, card);
                ResultSet rs = ps.executeQuery();

                if(rs.next()) {
                    bal = rs.getDouble("balance");
                }

                con.close();
            }
        } catch(Exception e) {
            System.err.println("Database balance fetch failed: " + e.getMessage());
        }
        
        // If database balance is 0 or failed, calculate from history
        if(bal == 0) {
            bal = calculateBalanceFromHistory();
        }
        
        return bal;
    }
    
    // Calculate balance from transaction history
    private double calculateBalanceFromHistory() {
        double calculatedBalance = 0; // Start with 0 for fresh calculation
        
        try {
            Connection con = DBConnection.getConnection();
            
            if(con != null) {
                PreparedStatement ps = con.prepareStatement(
                    "SELECT type, amount FROM transactions WHERE card_number=? ORDER BY date"
                );
                
                ps.setString(1, card);
                ResultSet rs = ps.executeQuery();
                
                while(rs.next()) {
                    String type = rs.getString("type");
                    double amount = rs.getDouble("amount");
                    
                    if("Deposit".equalsIgnoreCase(type)) {
                        calculatedBalance += amount;
                    } else if("Withdraw".equalsIgnoreCase(type)) {
                        calculatedBalance -= amount;
                    }
                }
                
                con.close();
            }
        } catch(Exception e) {
            System.err.println("History calculation failed: " + e.getMessage());
        }
        
        // If no transactions found, use initial balance
        if(calculatedBalance == 0) {
            calculatedBalance = balance;
        }
        
        return calculatedBalance;
    }

    // 💰 DEPOSIT
    public void deposit(double amt) throws Exception {

        if(amt <= 0) throw new Exception("Invalid amount");
        if(amt > 50000) throw new Exception("Max limit 50,000");

        // Add to local history for consistency
        history.add("Deposited ₹" + amt);

        try {
            Connection con = DBConnection.getConnection();
            
            if(con == null) {
                System.err.println("Database connection failed - using local mode");
                return;
            }

            // GET CURRENT BALANCE FIRST
            PreparedStatement getBalance = con.prepareStatement(
                "SELECT balance FROM users WHERE card_number=?"
            );
            getBalance.setString(1, card);
            ResultSet rs = getBalance.executeQuery();
            PreparedStatement ps1 = null;
            
            if(rs.next()) {
                double currentBalance = rs.getDouble("balance");
                
                // UPDATE BALANCE
                ps1 = con.prepareStatement(
                    "UPDATE users SET balance = ? WHERE card_number=?"
                );
                ps1.setDouble(1, currentBalance + amt);
                ps1.setString(2, card);
                int rowsUpdated = ps1.executeUpdate();
                
                System.out.println("Deposit update affected " + rowsUpdated + " rows for card: " + card);
            }
            
            rs.close();
            getBalance.close();
            
            // SAVE TRANSACTION
            PreparedStatement ps2 = con.prepareStatement(
                "INSERT INTO transactions(card_number,type,amount) VALUES(?,?,?)"
            );
            ps2.setString(1, card);
            ps2.setString(2, "Deposit");
            ps2.setDouble(3, amt);
            ps2.executeUpdate();
            ps2.close();
            
            if(ps1 != null) ps1.close();
            con.close();

        } catch(Exception e) {
            System.err.println("Deposit Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 💸 WITHDRAW
    public boolean withdraw(double amt) {

        try {
            Connection con = DBConnection.getConnection();
            
            if(con == null) {
                System.err.println("Database connection failed - using local mode");
                return false;
            }

            // 🔍 CHECK BALANCE
            PreparedStatement check = con.prepareStatement(
                "SELECT balance FROM users WHERE card_number=?"
            );
            check.setString(1, card);
            ResultSet rs = check.executeQuery();

            if(rs.next()) {
                double bal = rs.getDouble("balance");

                if(amt > bal) return false;

                // Add to local history for consistency
                history.add("Withdrawn ₹" + amt);

                // 🔥 UPDATE BALANCE
                PreparedStatement ps1 = con.prepareStatement(
                    "UPDATE users SET balance = balance - ? WHERE card_number=?"
                );
                ps1.setDouble(1, amt);
                ps1.setString(2, card);
                int rowsUpdated = ps1.executeUpdate();
                
                System.out.println("Withdrawal update affected " + rowsUpdated + " rows for card: " + card);

                // 🔥 SAVE TRANSACTION
                PreparedStatement ps2 = con.prepareStatement(
                    "INSERT INTO transactions(card_number,type,amount) VALUES(?,?,?)"
                );
                ps2.setString(1, card);
                ps2.setString(2, "Withdraw");
                ps2.setDouble(3, amt);
                ps2.executeUpdate();

                rs.close();
                check.close();
                ps1.close();
                ps2.close();
                con.close();

                return true;
            }
            
            rs.close();
            check.close();
            con.close();

        } catch(Exception e) {
            System.err.println("Withdraw Error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // 📊 HISTORY FROM DB
    public ArrayList<String> getHistory() {

        ArrayList<String> list = new ArrayList<>();

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) {
                System.err.println("Database unavailable; transaction history will not be loaded.");
                return list;
            }

            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM transactions WHERE card_number=? ORDER BY date DESC"
            );

            ps.setString(1, card);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                list.add(
                    rs.getString("type") + " ₹" +
                    rs.getDouble("amount") + " | " +
                    rs.getString("date")
                );
            }

            rs.close();
            ps.close();
            con.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // 🔥 RESET BALANCE (ADMIN)
    public void resetBalance(double newBalance) {

        try {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                "UPDATE users SET balance=? WHERE card_number=?"
            );

            ps.setDouble(1, newBalance);
            ps.setString(2, card);
            ps.executeUpdate();

            con.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}