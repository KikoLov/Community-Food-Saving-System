import java.sql.*;

public class CheckDB {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/food_saving?useSSL=false&serverTimezone=Asia/Shanghai",
                "root", "EA7music666");
            
            Statement stmt = conn.createStatement();
            
            // Check tables
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            System.out.println("Tables in database:");
            while(rs.next()) {
                System.out.println("  " + rs.getString(1));
            }
            
            // Check users
            rs = stmt.executeQuery("SELECT user_name, nick_name, user_type FROM biz_user");
            System.out.println("\nUsers in database:");
            while(rs.next()) {
                System.out.println("  " + rs.getString("user_name") + " - " + rs.getString("nick_name") + " (type: " + rs.getInt("user_type") + ")");
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
