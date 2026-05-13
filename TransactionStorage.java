import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class TransactionStorage {
    private static final String HISTORY_FILE = "transactions_history.txt";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // Save transaction to file
    public static void saveTransaction(String cardNumber, String type, double amount, double balance) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE, true))) {
            String timestamp = dateFormat.format(new Date());
            String transaction = String.format("%s|%s|%s|%.2f|%.2f%n", 
                timestamp, cardNumber, type, amount, balance);
            writer.write(transaction);
        } catch (IOException e) {
            System.err.println("Error saving transaction: " + e.getMessage());
        }
    }
    
    // Load all transactions for a card
    public static List<String> loadTransactions(String cardNumber) {
        List<String> transactions = new ArrayList<>();
        File file = new File(HISTORY_FILE);
        
        if (!file.exists()) {
            return transactions;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3 && parts[1].equals(cardNumber)) {
                    String timestamp = parts[0];
                    String type = parts[2];
                    String amount = parts.length > 3 ? parts[3] : "0.00";
                    String balance = parts.length > 4 ? parts[4] : "0.00";
                    
                    String formatted = String.format("%s %s ₹%s (Balance: ₹%s)", 
                        timestamp, type, amount, balance);
                    transactions.add(formatted);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
        }
        
        return transactions;
    }
    
    // Get last 10 transactions for mini statement
    public static List<String> getMiniStatement(String cardNumber) {
        List<String> allTransactions = loadTransactions(cardNumber);
        List<String> miniStatement = new ArrayList<>();
        
        // Get last 10 transactions (reverse order for recent first)
        for (int i = allTransactions.size() - 1; i >= Math.max(0, allTransactions.size() - 10); i--) {
            miniStatement.add(allTransactions.get(i));
        }
        
        return miniStatement;
    }
    
    // Clear transactions for a card
    public static void clearTransactions(String cardNumber) {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) return;
        
        List<String> otherTransactions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2 && !parts[1].equals(cardNumber)) {
                    otherTransactions.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading transactions: " + e.getMessage());
        }
        
        // Write back only the transactions for other cards
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE, false))) {
            for (String transaction : otherTransactions) {
                writer.write(transaction + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error clearing transactions: " + e.getMessage());
        }
    }
    
    // Get current balance from file (last transaction)
    public static double getCurrentBalance(String cardNumber) {
        List<String> transactions = loadTransactions(cardNumber);
        if (transactions.isEmpty()) {
            return 0.0;
        }
        
        // Get balance from the last transaction
        String lastTransaction = transactions.get(transactions.size() - 1);
        int balanceIndex = lastTransaction.lastIndexOf("Balance: ₹");
        if (balanceIndex != -1) {
            String balanceStr = lastTransaction.substring(balanceIndex + 10).trim();
            try {
                return Double.parseDouble(balanceStr);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        
        return 0.0;
    }
}
