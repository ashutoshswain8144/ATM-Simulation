import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LanguageFrame extends JFrame implements ActionListener {

    JButton english, hindi;
    BankAccount acc;

    public LanguageFrame(BankAccount acc) {
        this.acc = acc;

        setTitle(LanguageManager.getText("select_language"));
        setLayout(new GridLayout(3,1,10,10));

        JLabel label = new JLabel(LanguageManager.getText("select_language"), JLabel.CENTER);

        english = new JButton(LanguageManager.getText("english"));
        hindi = new JButton(LanguageManager.getText("hindi"));

        add(label);
        add(english);
        add(hindi);

        english.addActionListener(this);
        hindi.addActionListener(this);

        setSize(300,200);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == english) {
            LanguageManager.setLanguage(LanguageManager.ENGLISH);
        } else if(e.getSource() == hindi) {
            LanguageManager.setLanguage(LanguageManager.HINDI);
        }
        
        // Only create ATMFrame if account is not null
        if(acc != null) {
            new ATMFrame(acc);
        }
        dispose();
    }
}