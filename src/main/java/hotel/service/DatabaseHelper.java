package hotel.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles initialization of SQLite DB and provides JDBC Connections.
 */
public class DatabaseHelper {
    private static final String URL = "jdbc:sqlite:database.sqlite";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initDb() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Create Rooms
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "roomNumber TEXT PRIMARY KEY, " +
                    "roomType TEXT, " +
                    "pricePerDay REAL, " +
                    "available INTEGER)");

            // Create Customers
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "contactNumber TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "email TEXT)");

            // Create Bookings
            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                    "bookingId TEXT PRIMARY KEY, " +
                    "customerContact TEXT, " +
                    "roomNumber TEXT, " +
                    "checkIn TEXT, " +
                    "checkOut TEXT)");

            // Create Bills
            stmt.execute("CREATE TABLE IF NOT EXISTS bills (" +
                    "billId TEXT PRIMARY KEY, " +
                    "bookingId TEXT, " +
                    "totalAmount REAL, " +
                    "generationDate TEXT, " +
                    "isPaid INTEGER)");

        } catch (SQLException e) {
            System.err.println("[DB INIT ERROR] " + e.getMessage());
        }
    }
}
