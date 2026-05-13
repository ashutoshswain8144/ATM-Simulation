import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseTest {
    
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        
        try {
            // Test connection
            Connection con = DBConnection.getConnection();
            
            if (con != null) {
                System.out.println("✅ Database connected successfully!");
                
                // Test basic query
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT VERSION()");
                
                if (rs.next()) {
                    System.out.println("MySQL Version: " + rs.getString(1));
                }
                
                // Test if users table exists
                rs = stmt.executeQuery("SHOW TABLES LIKE 'users'");
                if (rs.next()) {
                    System.out.println("✅ Users table exists");
                    
                    // Check test users
                    rs = stmt.executeQuery("SELECT card_number, pin, balance FROM users");
                    System.out.println("\n--- Database Users ---");
                    while(rs.next()) {
                        System.out.println("Card: " + rs.getString("card_number") + 
                                         ", PIN: " + rs.getInt("pin") + 
                                         ", Balance: ₹" + rs.getDouble("balance"));
                    }
                } else {
                    System.out.println("❌ Users table not found - Please run database_setup.sql");
                }
                
                // Test if transactions table exists
                rs = stmt.executeQuery("SHOW TABLES LIKE 'transactions'");
                if (rs.next()) {
                    System.out.println("✅ Transactions table exists");
                } else {
                    System.out.println("❌ Transactions table not found - Please run database_setup.sql");
                }
                
                rs.close();
                stmt.close();
                con.close();
                
                System.out.println("\n✅ Database test completed successfully");
                
            } else {
                System.out.println("❌ Failed to connect to database");
                System.out.println("Please check:");
                System.out.println("1. MySQL server is running");
                System.out.println("2. Database 'atm_db' exists");
                System.out.println("3. Username/password are correct");
                System.out.println("4. MySQL JDBC driver is in classpath");
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
