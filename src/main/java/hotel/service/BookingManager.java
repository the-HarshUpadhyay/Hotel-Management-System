package hotel.service;

import hotel.model.*;
import javafx.collections.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BookingManager {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String DB_ERROR_PREFIX = "[DB]";

    private final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private final ObservableList<Booking> bookings = FXCollections.observableArrayList();
    private final ObservableList<Bill> bills = FXCollections.observableArrayList();
    private final AtomicInteger idCounter = new AtomicInteger(1000);
    private final AtomicInteger billCounter = new AtomicInteger(1000);

    public BookingManager() {
        DatabaseHelper.initDb();
        loadFromDatabase();
        if (rooms.isEmpty()) {
            seedSampleRooms();
        }
    }

    public ObservableList<Room> getRooms() { return rooms; }
    public ObservableList<Room> getAvailableRooms() {
        return rooms.stream().filter(Room::isAvailable)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
    public ObservableList<Booking> getBookings() { return bookings; }
    public ObservableList<Bill> getBills() { return bills; }

    public void addRoom(String roomNumber, RoomType type, double pricePerDay) {
        String trimmed = roomNumber.trim();
        if (trimmed.isEmpty() || pricePerDay <= 0) throw new IllegalArgumentException("Invalid room data.");
        if (rooms.stream().anyMatch(r -> r.getRoomNumber().equalsIgnoreCase(trimmed))) {
            throw new IllegalArgumentException("Room number exists.");
        }
        Room room = new Room(trimmed, type, pricePerDay, true);

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO rooms VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, room.getRoomNumber());
            stmt.setString(2, room.getRoomType().name());
            stmt.setDouble(3, room.getPricePerDay());
            stmt.setInt(4, 1);
            stmt.executeUpdate();
            rooms.add(room);
        } catch (SQLException e) {
            logSqlError("Add room", e);
        }
    }

    public Booking bookRoom(String customerName, String contactNumber, String email, Room room, LocalDate checkIn, LocalDate checkOut) {
        if (!room.isAvailable()) throw new IllegalArgumentException("Room is occupied.");

        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Please select both check-in and check-out dates.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date; same-day stay is not allowed.");
        }

        String trimmedContact = contactNumber == null ? "" : contactNumber.trim();
        if (!trimmedContact.matches("\\d{10}")) {
            throw new IllegalArgumentException("Contact number must be exactly 10 digits.");
        }

        Customer customer = new Customer(customerName.trim(), trimmedContact, email.trim());
        String bookingId = "BK" + idCounter.getAndIncrement();
        Booking booking = new Booking(bookingId, customer, room, checkIn, checkOut);

        try (Connection conn = DatabaseHelper.getConnection()) {
            try (PreparedStatement checkCust = conn.prepareStatement("SELECT contactNumber FROM customers WHERE contactNumber=?")) {
                checkCust.setString(1, customer.getContactNumber());
                if (!checkCust.executeQuery().next()) {
                    try (PreparedStatement insertCust = conn.prepareStatement("INSERT INTO customers VALUES (?, ?, ?)")) {
                        insertCust.setString(1, customer.getContactNumber());
                        insertCust.setString(2, customer.getName());
                        insertCust.setString(3, customer.getEmail());
                        insertCust.executeUpdate();
                    }
                }
            }

            try (PreparedStatement insertBooking = conn.prepareStatement("INSERT INTO bookings VALUES (?, ?, ?, ?, ?)")) {
                insertBooking.setString(1, bookingId);
                insertBooking.setString(2, customer.getContactNumber());
                insertBooking.setString(3, room.getRoomNumber());
                insertBooking.setString(4, checkIn.format(DATE_FMT));
                insertBooking.setString(5, checkOut.format(DATE_FMT));
                insertBooking.executeUpdate();
            }

            try (PreparedStatement updateRoom = conn.prepareStatement("UPDATE rooms SET available=0 WHERE roomNumber=?")) {
                updateRoom.setString(1, room.getRoomNumber());
                updateRoom.executeUpdate();
            }

            room.setAvailable(false);
            bookings.add(booking);
        } catch (SQLException e) {
            logSqlError("Book room", e);
        }

        return booking;
    }

    public Bill checkOutAndBill(Booking booking) {
        if (booking == null) return null;

        String billId = "INV" + billCounter.getAndIncrement();
        Bill bill = new Bill(billId, booking, booking.calculateTotalAmount(), LocalDate.now(), false);

        try (Connection conn = DatabaseHelper.getConnection()) {
            try (PreparedStatement insertBill = conn.prepareStatement(
                    "INSERT INTO bills (billId, bookingId, totalAmount, generationDate, isPaid, customerName, customerContact, customerEmail, roomNumber, roomType, checkIn, checkOut) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                insertBill.setString(1, bill.getBillId());
                insertBill.setString(2, booking.getBookingId());
                insertBill.setDouble(3, bill.getTotalAmount());
                insertBill.setString(4, bill.getGenerationDate().format(DATE_FMT));
                insertBill.setInt(5, 0); // Not paid
                insertBill.setString(6, booking.getCustomer().getName());
                insertBill.setString(7, booking.getCustomer().getContactNumber());
                insertBill.setString(8, booking.getCustomer().getEmail());
                insertBill.setString(9, booking.getRoom().getRoomNumber());
                insertBill.setString(10, booking.getRoom().getRoomType().name());
                insertBill.setString(11, booking.getCheckInDate().format(DATE_FMT));
                insertBill.setString(12, booking.getCheckOutDate().format(DATE_FMT));
                insertBill.executeUpdate();
            }

            try (PreparedStatement delBooking = conn.prepareStatement("DELETE FROM bookings WHERE bookingId=?")) {
                delBooking.setString(1, booking.getBookingId());
                delBooking.executeUpdate();
            }

            try (PreparedStatement updateRoom = conn.prepareStatement("UPDATE rooms SET available=1 WHERE roomNumber=?")) {
                updateRoom.setString(1, booking.getRoom().getRoomNumber());
                updateRoom.executeUpdate();
            }

            booking.getRoom().setAvailable(true);
            bookings.remove(booking);
            bills.add(bill);
        } catch (SQLException e) {
            logSqlError("Checkout and bill", e);
        }
        return bill;
    }

    public void markBillPaid(Bill bill) {
        if (bill == null) return;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE bills SET isPaid=1 WHERE billId=?")) {
            stmt.setString(1, bill.getBillId());
            stmt.executeUpdate();
            bill.setPaid(true);
            
            int idx = bills.indexOf(bill);
            if(idx != -1) bills.set(idx, bill);
        } catch (SQLException e) {
            logSqlError("Mark bill paid", e);
        }
    }

    public void deleteRoom(Room room) {
        if (!room.isAvailable()) throw new IllegalArgumentException("Cannot delete occupied room.");
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM rooms WHERE roomNumber=?")) {
            stmt.setString(1, room.getRoomNumber());
            stmt.executeUpdate();
            rooms.remove(room);
        } catch (SQLException e) {
            logSqlError("Delete room", e);
        }
    }

    private void loadFromDatabase() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM rooms")) {
                while (rs.next()) {
                    rooms.add(new Room(rs.getString("roomNumber"), RoomType.valueOf(rs.getString("roomType")), rs.getDouble("pricePerDay"), rs.getInt("available") == 1));
                }
            }

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT b.bookingId, b.customerContact, b.roomNumber, b.checkIn, b.checkOut, c.name, c.email FROM bookings b JOIN customers c ON b.customerContact = c.contactNumber")) {
                while (rs.next()) {
                    Customer cust = new Customer(rs.getString("name"), rs.getString("customerContact"), rs.getString("email"));
                    String rNum = rs.getString("roomNumber");
                    Room r = rooms.stream().filter(rm -> rm.getRoomNumber().equals(rNum)).findFirst().orElse(null);
                    if (r != null) {
                        bookings.add(new Booking(rs.getString("bookingId"), cust, r, LocalDate.parse(rs.getString("checkIn"), DATE_FMT), LocalDate.parse(rs.getString("checkOut"), DATE_FMT)));
                    }
                }
            }

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(
                    "SELECT billId, bookingId, totalAmount, generationDate, isPaid, customerName, customerContact, customerEmail, roomNumber, roomType, checkIn, checkOut FROM bills")) {
                while (rs.next()) {
                    bills.add(new Bill(
                            rs.getString("billId"),
                            createBillSnapshotBooking(rs),
                            rs.getDouble("totalAmount"),
                            LocalDate.parse(rs.getString("generationDate"), DATE_FMT),
                            rs.getBoolean("isPaid")));
                }
            }

            // Restore counters
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MAX(CAST(SUBSTR(bookingId, 3) AS INTEGER)) FROM bookings")) {
                if (rs.next() && rs.getInt(1) > 0) idCounter.set(rs.getInt(1) + 1);
            }
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MAX(CAST(SUBSTR(billId, 4) AS INTEGER)) FROM bills")) {
                if (rs.next() && rs.getInt(1) > 0) billCounter.set(rs.getInt(1) + 1);
            }
        } catch (SQLException e) {
            logSqlError("Load database", e);
        }
    }

    private void seedSampleRooms() {
        addRoom("101", RoomType.SINGLE, 800.0);
        addRoom("102", RoomType.SINGLE, 800.0);
        addRoom("201", RoomType.DOUBLE, 1400.0);
        addRoom("301", RoomType.DELUXE, 2500.0);
    }

    private Booking createBillSnapshotBooking(ResultSet rs) throws SQLException {
        String roomNumber = safeString(rs.getString("roomNumber"));
        Room existingRoom = rooms.stream()
                .filter(room -> room.getRoomNumber().equalsIgnoreCase(roomNumber))
                .findFirst()
                .orElse(null);

        RoomType roomType = parseRoomType(rs.getString("roomType"),
                existingRoom != null ? existingRoom.getRoomType() : RoomType.SINGLE);
        double pricePerDay = existingRoom != null ? existingRoom.getPricePerDay() : 0.0;

        Customer customer = new Customer(
                safeString(rs.getString("customerName")),
                safeString(rs.getString("customerContact")),
                safeString(rs.getString("customerEmail")));
        Room room = new Room(
                roomNumber,
                roomType,
                pricePerDay,
                false);
        return new Booking(
                safeString(rs.getString("bookingId")),
                customer,
                room,
                parseDateOrDefault(rs.getString("checkIn"), rs.getString("generationDate"), LocalDate.now()),
                parseDateOrDefault(rs.getString("checkOut"), rs.getString("generationDate"), LocalDate.now().plusDays(1)));
    }

    private void logSqlError(String context, SQLException e) {
        System.err.println(DB_ERROR_PREFIX + " " + context + ": " + e.getMessage());
    }

    private RoomType parseRoomType(String value, RoomType fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return RoomType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private LocalDate parseDateOrDefault(String value, String fallbackValue, LocalDate defaultValue) {
        if (value != null && !value.isBlank()) {
            return LocalDate.parse(value, DATE_FMT);
        }
        if (fallbackValue != null && !fallbackValue.isBlank()) {
            return LocalDate.parse(fallbackValue, DATE_FMT);
        }
        return defaultValue;
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }
}
