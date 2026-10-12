package database;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight, hand-written JDBC Connection Pool for low-spec server hardware.
 * Strictly caps active database connections to 5, reusing sockets via dynamic proxies.
 */
public class DBConnection {

    private static final int MAX_POOL_SIZE = 5;
    private static final long LEASE_TIMEOUT_MS = 3000;

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3307";
    private static final String DEFAULT_DB = "bus_reservation";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root@6069";

    private static final BlockingQueue<Connection> pool = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
    private static final AtomicInteger createdCount = new AtomicInteger(0);

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }

    private static String getJdbcUrl() {
        String host = getEnvOrDefault("DB_HOST", DEFAULT_HOST);
        String port = getEnvOrDefault("DB_PORT", DEFAULT_PORT);
        String dbName = getEnvOrDefault("DB_NAME", DEFAULT_DB);
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName +
                "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&connectTimeout=5000&socketTimeout=10000";
    }

    private static String getUser() {
        return getEnvOrDefault("DB_USER", DEFAULT_USER);
    }

    private static String getPassword() {
        return System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : DEFAULT_PASSWORD;
    }

    private static Connection createPhysicalConnection() {
        try {
            return DriverManager.getConnection(getJdbcUrl(), getUser(), getPassword());
        } catch (SQLException e) {
            System.err.println("[DBConnection] Failed to open physical connection: " + e.getMessage());
            return null;
        }
    }

    /**
     * Borrows a connection from the pool wrapped in a proxy that intercepts close()
     * to return the underlying physical connection back to the pool.
     */
    public static Connection getConnection() {
        Connection physicalCon = null;

        // 1. Try to take an idle connection from the pool
        physicalCon = pool.poll();

        // 2. If pool was empty but we haven't reached MAX_POOL_SIZE, create a new one
        if (physicalCon == null && createdCount.get() < MAX_POOL_SIZE) {
            if (createdCount.incrementAndGet() <= MAX_POOL_SIZE) {
                physicalCon = createPhysicalConnection();
                if (physicalCon == null) {
                    createdCount.decrementAndGet();
                }
            } else {
                createdCount.decrementAndGet();
            }
        }

        // 3. If still null, wait up to LEASE_TIMEOUT_MS for a connection to be returned
        if (physicalCon == null) {
            try {
                physicalCon = pool.poll(LEASE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        // 4. Validate physical connection; if broken, replace it
        if (physicalCon != null) {
            boolean valid = false;
            try {
                valid = !physicalCon.isClosed() && physicalCon.isValid(1);
            } catch (SQLException ignored) {}

            if (!valid) {
                try {
                    physicalCon.close();
                } catch (Exception ignored) {}
                createdCount.decrementAndGet();
                return getConnection(); // Retry once
            }
        } else {
            return null;
        }

        // 5. Wrap physical connection in proxy to intercept close()
        final Connection target = physicalCon;
        return (Connection) Proxy.newProxyInstance(
                DBConnection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new InvocationHandler() {
                    private boolean isClosed = false;

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("close".equals(name)) {
                            if (!isClosed) {
                                isClosed = true;
                                returnConnection(target);
                            }
                            return null;
                        }
                        if ("isClosed".equals(name)) {
                            return isClosed || target.isClosed();
                        }
                        if (isClosed) {
                            throw new SQLException("Connection was closed and returned to pool.");
                        }
                        return method.invoke(target, args);
                    }
                }
        );
    }

    private static void returnConnection(Connection con) {
        if (con == null) return;
        try {
            if (!con.isClosed() && con.isValid(1)) {
                if (!pool.offer(con)) {
                    con.close();
                    createdCount.decrementAndGet();
                }
            } else {
                con.close();
                createdCount.decrementAndGet();
            }
        } catch (SQLException e) {
            createdCount.decrementAndGet();
        }
    }

    /**
     * Closes all pooled connections on system shutdown.
     */
    public static void shutdown() {
        Connection con;
        while ((con = pool.poll()) != null) {
            try {
                con.close();
            } catch (Exception ignored) {}
        }
    }
}