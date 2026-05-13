import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ATMFrame extends JFrame implements ActionListener {

    JButton balance, deposit, withdraw, history, miniStatement, exit;
    BankAccount acc;

    // 🔥 Hindi Supported Font
    Font font = new Font("Nirmala UI", Font.BOLD, 16);

    ATMFrame(BankAccount acc) {

        this.acc = acc;

        setTitle(LanguageManager.getText("title"));

        getContentPane().setBackground(new Color(30, 30, 60));
        setLayout(new BorderLayout());

        // 🔷 Header
        JLabel title = new JLabel(LanguageManager.getText("title"), JLabel.CENTER);
        title.setFont(new Font("Nirmala UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(15,10,15,10));
        add(title, BorderLayout.NORTH);

        // 🔷 Main Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        balance = createButton(LanguageManager.getText("balance"));
        deposit = createButton(LanguageManager.getText("deposit"));
        withdraw = createButton(LanguageManager.getText("withdraw"));
        history = createButton(LanguageManager.getText("history"));
        miniStatement = createButton(LanguageManager.getText("ministatement"));
        exit = createButton(LanguageManager.getText("exit"));

        gbc.gridwidth = 1;
        mainPanel.add(balance, gbc);
        gbc.gridy++;
        mainPanel.add(deposit, gbc);
        gbc.gridy++;
        mainPanel.add(withdraw, gbc);
        gbc.gridy++;
        mainPanel.add(history, gbc);
        gbc.gridy++;
        mainPanel.add(miniStatement, gbc);
        gbc.gridy++;
        mainPanel.add(exit, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // 🔻 Card Info
        String maskedCard = acc.getCard().substring(0, 4) + "XXXX" + acc.getCard().substring(8);
        JLabel cardLabel = new JLabel("👤 Card: " + maskedCard, JLabel.CENTER);
        cardLabel.setForeground(Color.WHITE);
        cardLabel.setFont(font);
        add(cardLabel, BorderLayout.SOUTH);

        // Actions
        balance.addActionListener(this);
        deposit.addActionListener(this);
        withdraw.addActionListener(this);
        history.addActionListener(this);
        miniStatement.addActionListener(this);
        exit.addActionListener(this);

        setSize(400, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Button Creator
    // 🔷 Button Creator
    JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(font); // 🔥 Hindi supported font
        btn.setFocusPainted(false);
        btn.setBackground(new Color(0, 150, 255));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(180, 40));
        return btn;
    }

    public void actionPerformed(ActionEvent e) {

        // 💰 BALANCE
        if(e.getSource() == balance) {
            try {
                Connection con = DBConnection.getConnection();

                if(con == null) {
                    JOptionPane.showMessageDialog(this,
                        LanguageManager.getText("current_balance") + " ₹" + acc.getBalance());
                    return;
                }

                PreparedStatement ps = con.prepareStatement(
                    "SELECT balance FROM users WHERE card_number=?"
                );

                ps.setString(1, acc.getCard().trim());

                ResultSet rs = ps.executeQuery();

                if(rs.next()) {
                    double bal = rs.getDouble("balance");
                    JOptionPane.showMessageDialog(this,
                        LanguageManager.getText("current_balance") + " ₹" + bal);
                } else {
                    JOptionPane.showMessageDialog(this,
                        LanguageManager.getText("current_balance") + " ₹" + acc.getBalance());
                }

                con.close();

            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this,
                    LanguageManager.getText("current_balance") + " ₹" + acc.getBalance());
            }
        }

        // 💰 DEPOSIT
        if(e.getSource() == deposit) {

            String input = JOptionPane.showInputDialog(
                LanguageManager.getText("enter_amount") + " (Max: 50,000)");

            try {
                double amt = Double.parseDouble(input);

                if(amt <= 0 || amt > 50000) {
                    throw new Exception();
                }

                acc.deposit(amt);

                JOptionPane.showMessageDialog(this,
                    LanguageManager.getText("transaction_success") +
                    "\n" + LanguageManager.getText("current_balance") + ": ₹" + acc.getBalance());

            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this,
                    LanguageManager.getText("invalid_amount"));
            }
        }

        // 💸 WITHDRAW
        if(e.getSource() == withdraw) {

            String input = JOptionPane.showInputDialog(
                LanguageManager.getText("enter_amount"));

            try {
                double amt = Double.parseDouble(input);

                if(amt <= 0) throw new Exception();

                if(!acc.withdraw(amt)) {
                    JOptionPane.showMessageDialog(this,
                        LanguageManager.getText("insufficient_balance"));
                } else {
                    JOptionPane.showMessageDialog(this,
                        LanguageManager.getText("transaction_success"));
                }

            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this,
                    LanguageManager.getText("invalid_amount"));
            }
        }

        // 📊 HISTORY
        if(e.getSource() == history) {
            String h = "";
            for(String s : acc.getHistory()) {
                h += s + "\n";
            }

            JOptionPane.showMessageDialog(this,
                h.isEmpty() ? LanguageManager.getText("no_transactions") : h);
        }

        // MINI STATEMENT
        if(e.getSource() == miniStatement) {
            MiniStatement.generate(acc.getCard());
        }

        // EXIT
        if(e.getSource() == exit) {
            System.exit(0);
        }
    }
}