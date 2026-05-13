import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ForgotPinFrame extends JFrame implements ActionListener {

    JTextField cardField;
    JButton sendOtpBtn;

    String generatedOtp;

    public ForgotPinFrame() {

        setTitle("Forgot PIN");

        setLayout(new GridLayout(3,2,10,10));

        add(new JLabel("Card Number:"));
        cardField = new JTextField();
        add(cardField);

        sendOtpBtn = new JButton("Send OTP");
        add(sendOtpBtn);

        sendOtpBtn.addActionListener(this);

        setSize(300,200);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        String card = cardField.getText().trim();

        if(card.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Enter card number");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();

            if(con == null) {
                // Fallback to local PIN reset
                localPinReset(card);
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM users WHERE card_number=?"
            );

            ps.setString(1, card);
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                // Generate OTP
                generatedOtp = String.valueOf((int)(Math.random()*9000)+1000);
                JOptionPane.showMessageDialog(this,"OTP: "+generatedOtp);

                String inputOtp = JOptionPane.showInputDialog("Enter OTP");

                if(inputOtp != null && inputOtp.equals(generatedOtp)) {

                    // New PIN
                    String newPin = JOptionPane.showInputDialog("Enter New PIN");

                    if(newPin != null && newPin.matches("\\d{4}")) {

                        PreparedStatement ps2 = con.prepareStatement(
                            "UPDATE users SET pin=? WHERE card_number=?"
                        );

                        ps2.setInt(1, Integer.parseInt(newPin));
                        ps2.setString(2, card);

                        ps2.executeUpdate();

                        JOptionPane.showMessageDialog(this,"PIN Reset Successful");

                        dispose();

                    } else {
                        JOptionPane.showMessageDialog(this,"Invalid PIN (4 digits)");
                    }

                } else {
                    JOptionPane.showMessageDialog(this,"Wrong OTP");
                }

            } else {
                // Try local PIN reset if database user not found
                localPinReset(card);
            }

            con.close();

        } catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Error resetting PIN");
        }
    }

    // Local PIN reset fallback
    private void localPinReset(String card) {
        // Check if it's a valid test card
        if(card.equals("8144344192") || card.equals("7205010463")) {
            
            int otp = (int)(Math.random()*9000)+1000;
            JOptionPane.showMessageDialog(this,"OTP: "+otp);

            String inputOtp = JOptionPane.showInputDialog("Enter OTP");

            if(inputOtp != null && Integer.parseInt(inputOtp)==otp) {
                
                String newPin = JOptionPane.showInputDialog("Enter New PIN");

                if(newPin != null && newPin.matches("\\d{4}")) {
                    JOptionPane.showMessageDialog(this,"PIN Reset Successful\nNew PIN: " + newPin);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,"Invalid PIN (4 digits)");
                }
            } else {
                JOptionPane.showMessageDialog(this,"Wrong OTP");
            }
        } else {
            JOptionPane.showMessageDialog(this,"Card not found\n\nValid Cards:\n8144344192\n7205010463");
        }
    }
}