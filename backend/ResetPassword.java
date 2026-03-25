import java.sql.*;

public class ResetPassword {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/food_saving?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
                "root", "EA7music666");
            
            Statement stmt = conn.createStatement();
            
            // Check existing users
            ResultSet rs = stmt.executeQuery("SELECT user_id, user_name, password FROM biz_user");
            System.out.println("Existing users:");
            while(rs.next()) {
                System.out.println("  ID: " + rs.getInt("user_id") + ", Username: " + rs.getString("user_name") + ", Password: " + rs.getString("password"));
            }
            
            // Delete all users and re-create
            stmt.execute("DELETE FROM biz_user WHERE user_name IN ('admin', 'consumer', 'merchant')");
            stmt.execute("DELETE FROM biz_user_profile WHERE user_id IN (SELECT user_id FROM biz_user WHERE user_name IN ('admin', 'consumer', 'merchant'))");
            
            // Insert users with known BCrypt password for "admin"
            // BCrypt hash for "admin": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
            String pwdHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
            
            stmt.execute("INSERT INTO biz_user (user_name, password, nick_name, user_type, status, del_flag) VALUES " +
                "('admin', '" + pwdHash + "', 'Administrator', 3, 0, 0)," +
                "('consumer', '" + pwdHash + "', 'Consumer User', 1, 0, 0)," +
                "('merchant', '" + pwdHash + "', 'Merchant User', 2, 0, 0)");
            
            // Create user profiles
            stmt.execute("INSERT INTO biz_user_profile (user_id, carbon_points, total_carbon_saved, total_food_saved) " +
                "SELECT user_id, 0, 0, 0 FROM biz_user WHERE user_name IN ('admin', 'consumer', 'merchant')");
            
            System.out.println("\nUsers reset successfully!");
            System.out.println("Username: admin, Password: admin");
            System.out.println("Username: consumer, Password: admin");
            System.out.println("Username: merchant, Password: admin");
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
