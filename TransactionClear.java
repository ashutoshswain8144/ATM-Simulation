import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class TransactionClear extends JFrame implements ActionListener {
    
    JTextField cardField;
    JTextField pinField;
    JButton clearBtn;
    
    public TransactionClear() {
        setTitle("Clear Transactions");

        setLayout(new GridLayout(4, 2, 10, 10));
        
        add(new JLabel("Card Number:"));
        cardField = new JTextField();
        add(cardField);
        
        add(new JLabel("PIN:"));
        pinField = new JTextField();
        add(pinField);
        
        clearBtn = new JButton("Clear Transactions & Reset Balance");
        clearBtn.addActionListener(this);
        add(clearBtn);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        add(closeBtn);
        
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        String card = cardField.getText().trim();
        String pinText = pinField.getText().trim();
        
        if(card.isEmpty() || pinText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Card & PIN");
            return;
        }
        
        try {
            int pin = Integer.parseInt(pinText);
            
            Connection con = DBConnection.getConnection();
            
            // 🔍 CHECK USER
            PreparedStatement check = con.prepareStatement(
                "SELECT * FROM users WHERE card_number=? AND pin=?"
            );
            
            check.setString(1, card);
            check.setInt(2, pin);
            
            ResultSet rs = check.executeQuery();
            
            if(rs.next()) {
                
                // 🔥 DELETE TRANSACTIONS
                PreparedStatement delete = con.prepareStatement(
                    "DELETE FROM transactions WHERE card_number=?"
                );
                delete.setString(1, card);
                delete.executeUpdate();
                
                // 🔥 RESET BALANCE (DEFAULT)
                double resetBalance = 5000; // default
                
                if(card.equals("8144344192")) {
                    resetBalance = 10000;
                } else if(card.equals("7205010463")) {
                    resetBalance = 5000;
                }
                
                PreparedStatement update = con.prepareStatement(
                    "UPDATE users SET balance=? WHERE card_number=?"
                );
                
                update.setDouble(1, resetBalance);
                update.setString(2, card);
                update.executeUpdate();
                
                JOptionPane.showMessageDialog(this,
                    "✅ Transactions Cleared\n" +
                    "✅ Balance Reset\n" +
                    "New Balance: ₹" + resetBalance
                );
                
                con.close();
                
                cardField.setText("");
                pinField.setText("");
                
            } else {
                JOptionPane.showMessageDialog(this, "❌ Invalid Card or PIN");
            }
            
        } catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    public static void main(String[] args) {
        new TransactionClear();
    }
}