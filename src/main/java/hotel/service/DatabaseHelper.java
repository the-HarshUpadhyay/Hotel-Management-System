package hotel.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String DEFAULT_URL = "jdbc:sqlite:database.sqlite";
    private static final String DB_URL = valueOrDefault(System.getenv("HOTEL_DB_URL"), DEFAULT_URL);
    private static final String DB_USER = System.getenv("HOTEL_DB_USER");
    private static final String DB_PASSWORD = System.getenv("HOTEL_DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        if (DB_USER != null && !DB_USER.isBlank()) {
            return DriverManager.getConnection(DB_URL, DB_USER, valueOrDefault(DB_PASSWORD, ""));
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void initDb() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "roomNumber VARCHAR(50) PRIMARY KEY, " +
                    "roomType VARCHAR(50), " +
                    "pricePerDay DOUBLE, " +
                    "available BOOLEAN)");

            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "contactNumber VARCHAR(50) PRIMARY KEY, " +
                    "name VARCHAR(255), " +
                    "email VARCHAR(255))");

            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                    "bookingId VARCHAR(50) PRIMARY KEY, " +
                    "customerContact VARCHAR(50), " +
                    "roomNumber VARCHAR(50), " +
                    "checkIn VARCHAR(20), " +
                    "checkOut VARCHAR(20))");

            stmt.execute("CREATE TABLE IF NOT EXISTS bills (" +
                    "billId VARCHAR(50) PRIMARY KEY, " +
                    "bookingId VARCHAR(50), " +
                    "totalAmount DOUBLE, " +
                    "generationDate VARCHAR(20), " +
                    "isPaid BOOLEAN)");

            stmt.execute("CREATE TABLE IF NOT EXISTS app_texts (" +
                    "sectionName VARCHAR(100) NOT NULL, " +
                    "keyName VARCHAR(100) NOT NULL, " +
                    "value VARCHAR(4000), " +
                    "PRIMARY KEY (sectionName, keyName))");

            ensureColumnExists(conn, "bills", "customerName", "VARCHAR(255)");
            ensureColumnExists(conn, "bills", "customerContact", "VARCHAR(50)");
            ensureColumnExists(conn, "bills", "customerEmail", "VARCHAR(255)");
            ensureColumnExists(conn, "bills", "roomNumber", "VARCHAR(50)");
            ensureColumnExists(conn, "bills", "roomType", "VARCHAR(50)");
            ensureColumnExists(conn, "bills", "checkIn", "VARCHAR(20)");
            ensureColumnExists(conn, "bills", "checkOut", "VARCHAR(20)");

        } catch (SQLException e) {
            System.err.println("[DB INIT ERROR] " + e.getMessage());
        }
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static void ensureColumnExists(Connection conn, String tableName, String columnName, String definition)
            throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            if (columns.next()) {
                return;
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }
}
