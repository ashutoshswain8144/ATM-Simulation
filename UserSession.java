import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserSession {
    private static final String SESSION_FILE = "user_session.dat";
    private static Map<String, String> currentSession = new HashMap<>();
    
    // Save user session
    public static void saveSession(String cardNumber, String pin, double balance) {
        try {
            currentSession.clear(); // Clear previous session
            currentSession.put("cardNumber", cardNumber);
            currentSession.put("pin", pin);
            currentSession.put("balance", String.valueOf(balance));
            currentSession.put("loginTime", String.valueOf(System.currentTimeMillis()));
            
            // Save to file
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SESSION_FILE))) {
                oos.writeObject(currentSession);
            }
        } catch (IOException e) {
            System.err.println("Error saving session: " + e.getMessage());
        }
    }
    
    // Load user session
    public static Map<String, String> loadSession() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SESSION_FILE))) {
            @SuppressWarnings("unchecked")
            Map<String, String> session = (Map<String, String>) ois.readObject();
            currentSession.clear(); // Clear and update current session
            currentSession.putAll(session);
            return session;
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
    
    // Get current session
    public static Map<String, String> getCurrentSession() {
        return currentSession;
    }
    
    // Check if session exists
    public static boolean hasSession() {
        return !currentSession.isEmpty() || new File(SESSION_FILE).exists();
    }
    
    // Clear session
    public static void clearSession() {
        currentSession.clear();
        File sessionFile = new File(SESSION_FILE);
        if (sessionFile.exists()) {
            sessionFile.delete();
            System.out.println("Session cleared");
        }
    }
    
    // Get session data
    public static String getCardNumber() {
        return currentSession.get("cardNumber");
    }
    
    public static String getPin() {
        return currentSession.get("pin");
    }
    
    public static double getBalance() {
        String balanceStr = currentSession.get("balance");
        return balanceStr != null ? Double.parseDouble(balanceStr) : 0.0;
    }
}
