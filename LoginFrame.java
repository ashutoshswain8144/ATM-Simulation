import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;

public class LoginFrame extends JFrame implements ActionListener {

    private static final int MAX_LOGIN_ATTEMPTS = 3;

    JTextField cardField;
    JPasswordField pinField;
    JButton loginBtn, forgotBtn;

    Font font = new Font("Nirmala UI", Font.PLAIN, 16);
    private final Map<String, Integer> failedAttempts = new HashMap<>();
    private final Set<String> blockedCards = new HashSet<>();

    LoginFrame() {

        // 🔥 Clear old session
        UserSession.clearSession();

        UIManager.put("OptionPane.messageFont", font);
        UIManager.put("OptionPane.buttonFont", font);
        UIManager.put("TextField.font", font);

        setTitle(LanguageManager.getText("title"));

        getContentPane().setBackground(Color.LIGHT_GRAY);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel cardLabel = new JLabel(LanguageManager.getText("card_number"));
        cardLabel.setFont(font);
        mainPanel.add(cardLabel, gbc);

        cardField = new JTextField(12);
        cardField.setFont(font);
        gbc.gridx = 1;
        mainPanel.add(cardField, gbc);

        gbc.gridx = 2;
        JLabel pinLabel = new JLabel(LanguageManager.getText("pin"));
        pinLabel.setFont(font);
        mainPanel.add(pinLabel, gbc);

        pinField = new JPasswordField(4);
        pinField.setFont(font);
        gbc.gridx = 3;
        mainPanel.add(pinField, gbc);

        loginBtn = new JButton(LanguageManager.getText("login"));
        loginBtn.setFont(font);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        mainPanel.add(loginBtn, gbc);

        forgotBtn = new JButton(LanguageManager.getText("forgot_pin"));
        forgotBtn.setFont(font);
        gbc.gridy = 2;
        mainPanel.add(forgotBtn, gbc);

        add(mainPanel, BorderLayout.CENTER);

        loginBtn.addActionListener(this);
        forgotBtn.addActionListener(e -> new ForgotPinFrame());

        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent e) {

        String card = cardField.getText().trim();

        // ✅ CARD VALIDATION (10 digit only)
        if (!card.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this,
                    "Card number must be exactly 10 digits");
            return;
        }

        if (isBlocked(card)) {
            JOptionPane.showMessageDialog(this,
                    "This account is blocked after 3 failed login attempts.");
            return;
        }

        try {
            String pinText = new String(pinField.getPassword());

            // ✅ PIN VALIDATION (4 digit only)
            if (!pinText.matches("\\d{4}")) {
                JOptionPane.showMessageDialog(this,
                        "PIN must be exactly 4 digits");
                return;
            }

            int pin = Integer.parseInt(pinText);

            Connection con = DBConnection.getConnection();

            if (con == null) {
                localAuthentication(card, pin);
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "SELECT pin, balance FROM users WHERE card_number=?"
            );

            ps.setString(1, card);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int storedPin = rs.getInt("pin");

                if (storedPin == pin) {
                    failedAttempts.remove(card);

                    int otp = (int)(Math.random() * 9000) + 1000;
                    JOptionPane.showMessageDialog(this, "OTP: " + otp);

                    JTextField otpField = new JTextField();
                    otpField.setFont(font);

                    Object[] message = {"Enter OTP:", otpField};

                    int option = JOptionPane.showConfirmDialog(
                            this, message, "OTP",
                            JOptionPane.OK_CANCEL_OPTION
                    );

                    if (option == JOptionPane.OK_OPTION &&
                            !otpField.getText().isEmpty() &&
                            Integer.parseInt(otpField.getText()) == otp) {

                        double balance = rs.getDouble("balance");

                        BankAccount acc = new BankAccount(card, pin, balance);

                        UserSession.saveSession(card, String.valueOf(pin), balance);

                        new LanguageFrame(acc);
                        dispose();

                    } else {
                        JOptionPane.showMessageDialog(this, "Wrong OTP");
                    }
                } else {
                    int remaining = recordFailedAttempt(card);
                    JOptionPane.showMessageDialog(this,
                            remaining > 0
                                    ? "Wrong PIN. " + remaining + " attempt(s) remaining."
                                    : "This account is blocked after 3 failed login attempts.");
                }
            } else {

                // ✅ NEW USER
                double initialBalance = 0;

                PreparedStatement insertPs = con.prepareStatement(
                        "INSERT INTO users (card_number, pin, balance) VALUES (?, ?, ?)"
                );

                insertPs.setString(1, card);
                insertPs.setInt(2, pin);
                insertPs.setDouble(3, initialBalance);
                insertPs.executeUpdate();

                BankAccount acc = new BankAccount(card, pin, initialBalance);

                UserSession.saveSession(card, String.valueOf(pin), initialBalance);

                new LanguageFrame(acc);
                dispose();
            }

            con.close();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid PIN");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error");
        }
    }

    private void localAuthentication(String card, int pin) {

        int otp = (int)(Math.random() * 9000) + 1000;
        JOptionPane.showMessageDialog(this, "OTP: " + otp);

        JTextField otpField = new JTextField();
        otpField.setFont(font);

        Object[] message = {"Enter OTP:", otpField};

        int option = JOptionPane.showConfirmDialog(
                this, message, "OTP",
                JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION &&
                !otpField.getText().isEmpty() &&
                Integer.parseInt(otpField.getText()) == otp) {

            double initialBalance = 10000;

            BankAccount acc = new BankAccount(card, pin, initialBalance);

            UserSession.saveSession(card, String.valueOf(pin), initialBalance);

            new LanguageFrame(acc);
            dispose();

        } else {
            JOptionPane.showMessageDialog(this, "Wrong OTP");
        }
    }

    private boolean isBlocked(String card) {
        return blockedCards.contains(card);
    }

    private int recordFailedAttempt(String card) {
        int attempts = failedAttempts.getOrDefault(card, 0) + 1;
        failedAttempts.put(card, attempts);

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            blockedCards.add(card);
            return 0;
        }

        return MAX_LOGIN_ATTEMPTS - attempts;
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}
