import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropTableScript {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/vehicle_booking_db?serverTimezone=UTC";
        String user = "root";
        String password = "P@ssw0rd6633";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate("DROP TABLE IF EXISTS inspections;");
            System.out.println("Table 'inspections' dropped successfully.");
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
