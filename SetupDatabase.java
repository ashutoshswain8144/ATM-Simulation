import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class SetupDatabase {
    
    public static void main(String[] args) {
        System.out.println("Setting up ATM database...");
        
        try {
            Connection con = DBConnection.getConnection();
            
            if (con != null) {
                Statement stmt = con.createStatement();
                
                // Create database
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS atm_db");
                stmt.executeUpdate("USE atm_db");
                
                // Create users table
                String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "card_number VARCHAR(20) PRIMARY KEY, " +
                    "pin INT NOT NULL, " +
                    "balance DECIMAL(10,2) DEFAULT 0.00" +
                    ")";
                stmt.executeUpdate(createUsersTable);
                System.out.println("✅ Users table created");
                
                // Create transactions table
                String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "card_number VARCHAR(20) NOT NULL, " +
                    "type VARCHAR(10) NOT NULL, " +
                    "amount DECIMAL(10,2) NOT NULL, " +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                stmt.executeUpdate(createTransactionsTable);
                System.out.println("✅ Transactions table created");
                
                // Insert test users
                String insertUsers = "INSERT INTO users (card_number, pin, balance) VALUES " +
                    "('8144344192', 1234, 10000.00), " +
                    "('7205010463', 1111, 5000.00) " +
                    "ON DUPLICATE KEY UPDATE balance=VALUES(balance)";
                stmt.executeUpdate(insertUsers);
                System.out.println("✅ Test users inserted");
                
                // Verify users
                var rs = stmt.executeQuery("SELECT card_number, pin, balance FROM users");
                System.out.println("\n--- Database Users ---");
                while(rs.next()) {
                    System.out.println("Card: " + rs.getString("card_number") + 
                                     ", PIN: " + rs.getInt("pin") + 
                                     ", Balance: ₹" + rs.getDouble("balance"));
                }
                
                stmt.close();
                con.close();
                
                System.out.println("\n✅ Database setup completed successfully!");
                System.out.println("Now you can run: java LoginFrame");
                
            } else {
                System.out.println("❌ Cannot connect to database");
                System.out.println("Please check MySQL connection details in DBConnection.java");
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Database setup error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
