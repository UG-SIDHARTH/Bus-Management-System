package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Automatic Database Migrator for SwiftBus.
 * Ensures all tables and required columns for User Accounts, Multi-booking,
 * Disability Registry, and Gender-aware Seating exist.
 */
public class DBMigration {

    public static void migrate() {
        System.out.println("[DBMigration] Checking database schema for v3.0 features...");
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                System.out.println("[DBMigration] Database not connected; skipping auto-migration.");
                return;
            }

            try (Statement st = con.createStatement()) {
                DatabaseMetaData meta = con.getMetaData();

                // 1. Ensure passenger_disability table exists
                st.execute("CREATE TABLE IF NOT EXISTS passenger_disability (" +
                        "    disability_reg_id INT NOT NULL AUTO_INCREMENT," +
                        "    passenger_id INT NOT NULL," +
                        "    disability_type VARCHAR(100) NOT NULL," +
                        "    certificate_no VARCHAR(50) NOT NULL," +
                        "    concession_percentage DOUBLE NOT NULL DEFAULT 50.0," +
                        "    verified_status VARCHAR(20) NOT NULL DEFAULT 'VERIFIED'," +
                        "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "    PRIMARY KEY (disability_reg_id)," +
                        "    KEY fk_disability_passenger (passenger_id)," +
                        "    CONSTRAINT fk_disability_passenger FOREIGN KEY (passenger_id) REFERENCES passenger (passenger_id) ON DELETE CASCADE" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

                // 2. Check and add columns to 'passenger' table
                addColumnIfNotExists(meta, st, "passenger", "is_disability", "TINYINT(1) NOT NULL DEFAULT 0");
                addColumnIfNotExists(meta, st, "passenger", "disability_type", "VARCHAR(100) NULL");
                addColumnIfNotExists(meta, st, "passenger", "disability_id", "VARCHAR(50) NULL");

                // 3. Check and add columns to 'booking' table
                addColumnIfNotExists(meta, st, "booking", "concession_amount", "DOUBLE NOT NULL DEFAULT 0.0");
                addColumnIfNotExists(meta, st, "booking", "is_concession", "TINYINT(1) NOT NULL DEFAULT 0");

                // 4. Seed test1 to test5 test accounts (password: 1234)
                st.execute("INSERT IGNORE INTO users (id, username, email, password_hash, salt, role) VALUES " +
                        "(3, 'test1', 'test1@swiftbus.local', 'c49d61715c488c16c308ffa2aac8bf78265a6a418ad7b79dce96ffa62b4a8b28', '2acfec13c3b1548fc74ca21496de8f7e', 'USER'), " +
                        "(4, 'test2', 'test2@swiftbus.local', '90ea0553f4c66a7f097db59e216a4a8ab7af0cc60441abcdf5630087828434b0', '2af96a45d3d19bbf76e9c7d52728f9e0', 'USER'), " +
                        "(5, 'test3', 'test3@swiftbus.local', 'b13f1f69b9bc542ba6d9c68e8c0a455456d4790763c1f8629f3d2791c23e69e8', '06ae62b3adba6e9cac924438aa501325', 'USER'), " +
                        "(6, 'test4', 'test4@swiftbus.local', '7a2de55354226f2c3c8a714bcdde8b25dbbf7bfd79f37be2f7653d4f340e0eed', 'a8abd5e482785b1a979e97c987f5430d', 'USER'), " +
                        "(7, 'test5', 'test5@swiftbus.local', '2af439a4c4b5565de8a196f00f99fbc16759c43a195b0ddf6ad1cbae156d9658', '956dccb74876f321b9bdfaad4bc17701', 'USER')");

                System.out.println("[DBMigration] Schema migration completed successfully.");
            }
        } catch (SQLException e) {
            System.err.println("[DBMigration] Schema migration warning: " + e.getMessage());
        }
    }

    private static void addColumnIfNotExists(DatabaseMetaData meta, Statement st, String tableName, String columnName, String columnDef) {
        try {
            boolean exists = false;
            try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
                if (rs.next()) {
                    exists = true;
                }
            }
            if (!exists) {
                System.out.println("[DBMigration] Adding column " + columnName + " to table " + tableName + "...");
                st.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDef);
            }
        } catch (SQLException e) {
            // Column may already exist or table doesn't exist yet
            System.err.println("[DBMigration] Note on column " + columnName + ": " + e.getMessage());
        }
    }
}
