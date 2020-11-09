import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final static String url = "jdbc:postgresql://127.0.0.1:5432/shop";
    private final static String user = "postgres";
    private final static String password = "root";
    private Connection con;
    public DBConnection() throws SQLException {
        con = DriverManager.getConnection(url, user, password);
    }

    public Connection getConnection() {
        return con;
    }

}
