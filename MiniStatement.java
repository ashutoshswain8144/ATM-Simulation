import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MiniStatement {
    
    public static void generate(String cardNumber) {
        JFrame statementFrame = new JFrame("Mini Statement");
        statementFrame.setSize(500, 400);
        statementFrame.setLocationRelativeTo(null);
        statementFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel titleLabel = new JLabel(LanguageManager.getText("ministatement"), JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Transaction area
        JTextArea transactionArea = new JTextArea();
        transactionArea.setEditable(false);
        transactionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        transactionArea.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // Header
        StringBuilder transactions = new StringBuilder();
        transactions.append(String.format("%-20s %-10s %-10s %s\n", "DATE", "TYPE", "AMOUNT", "BALANCE"));
        transactions.append("--------------------------------------------------\n");
        
        // Get transactions from database
        try {
            Connection con = DBConnection.getConnection();
            
            if(con != null) {
                PreparedStatement ps = con.prepareStatement(
                    "SELECT type, amount, date FROM transactions WHERE card_number=? ORDER BY date DESC LIMIT 10"
                );
                ps.setString(1, cardNumber);
                ResultSet rs = ps.executeQuery();
                
                while(rs.next()) {
                    String type = rs.getString("type");
                    double amount = rs.getDouble("amount");
                    String date = rs.getTimestamp("date").toString();
                    
                    // Format date to show only date and time
                    String formattedDate = date.substring(0, date.indexOf('.'));
                    
                    transactions.append(String.format("%-20s %-10s %-10.2f\n", 
                        formattedDate, type, amount));
                }
                
                rs.close();
                ps.close();
                con.close();
            } else {
                transactions.append("Database connection failed\n");
            }
        } catch(Exception e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
            transactions.append("Error loading transactions\n");
        }
        
        if(transactions.length() == 0) {
            transactions.append(LanguageManager.getText("no_transactions") + "\n");
        }
        
        transactionArea.setText(transactions.toString());
        
        // Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // Get current balance from database
        double currentBalance = 0;
        try {
            Connection con = DBConnection.getConnection();
            if(con != null) {
                PreparedStatement ps = con.prepareStatement(
                    "SELECT balance FROM users WHERE card_number=?"
                );
                ps.setString(1, cardNumber);
                ResultSet rs = ps.executeQuery();
                
                if(rs.next()) {
                    currentBalance = rs.getDouble("balance");
                }
                
                rs.close();
                ps.close();
                con.close();
            }
        } catch(Exception e) {
            System.err.println("Error getting balance: " + e.getMessage());
        }
        
        JLabel balanceLabel = new JLabel(LanguageManager.getText("current_balance") + " ₹" + 
            String.format("%.2f", currentBalance), JLabel.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        balanceLabel.setForeground(Color.BLUE);
        
        JLabel thankYouLabel = new JLabel(LanguageManager.getText("thank_you"), JLabel.CENTER);
        thankYouLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        
        footerPanel.add(balanceLabel, BorderLayout.NORTH);
        footerPanel.add(thankYouLabel, BorderLayout.CENTER);
        
        // Scroll pane for transactions
        JScrollPane scrollPane = new JScrollPane(transactionArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        // Print button
        JButton printButton = new JButton(LanguageManager.getText("print"));
        printButton.addActionListener(e -> {
            try {
                transactionArea.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(statementFrame, "Print failed: " + ex.getMessage());
            }
        });
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> statementFrame.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        statementFrame.add(mainPanel);
        statementFrame.setVisible(true);
    }
}
