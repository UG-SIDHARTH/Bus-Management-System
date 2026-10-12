package database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "bus_reservation";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root@6069";

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }

    public static Connection getConnection() {

        Connection con = null;

        String host = getEnvOrDefault("DB_HOST", DEFAULT_HOST);
        String port = getEnvOrDefault("DB_PORT", DEFAULT_PORT);
        String dbName = getEnvOrDefault("DB_NAME", DEFAULT_DB);
        String user = getEnvOrDefault("DB_USER", DEFAULT_USER);
        String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : DEFAULT_PASSWORD;

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";

        try {
            con = DriverManager.getConnection(url, user, password);
            System.out.println("Database Connected Successfully (" + host + ":" + port + "/" + dbName + ")");

        } catch (Exception e) {
            System.out.println("Database Connection Failed (" + url + ")");
            e.printStackTrace();
        }

        return con;
    }
}