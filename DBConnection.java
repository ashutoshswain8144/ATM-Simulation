import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    private static final String URL = "jdbc:mysql://localhost:3306/atm_db";
    private static final String USER = "root";
    private static final String PASSWORD = "root123";
    private static boolean connectionAttempted = false;
    private static boolean databaseAvailable = false;
    
    public static synchronized Connection getConnection() {
        if (connectionAttempted && !databaseAvailable) {
            return null;
        }

        connectionAttempted = true;
        try {
            // Try to load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            databaseAvailable = true;
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Database features will be disabled.");
            System.err.println("To enable database features:");
            System.err.println("1. Download MySQL Connector/J from: https://dev.mysql.com/downloads/connector/j/");
            System.err.println("2. Add mysql-connector-java-x.x.xx.jar to classpath");
            databaseAvailable = false;
            return null;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            System.err.println("Please check:");
            System.err.println("1. MySQL server is running on localhost:3306");
            System.err.println("2. Database 'atm_db' exists");
            System.err.println("3. Username/password are correct");
            databaseAvailable = false;
            return null;
        }
    }
}
