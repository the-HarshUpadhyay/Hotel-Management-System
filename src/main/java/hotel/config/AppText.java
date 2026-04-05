package hotel.config;

import hotel.service.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class AppText {

    private static final AppText INSTANCE = new AppText();
    private static final Locale INDIAN_LOCALE = new Locale("en", "IN");

    private final Map<String, String> values = new LinkedHashMap<>();

    private AppText() {
        DatabaseHelper.initDb();
        seedDefaults();
        loadFromDatabase();
    }

    public static AppText get() {
        return INSTANCE;
    }

    public String text(String section, String key, String fallback) {
        return values.getOrDefault(composeKey(section, key), fallback);
    }

    public String format(String section, String key, String fallback, Map<String, String> tokens) {
        String template = text(section, key, fallback);
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            template = template.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return template;
    }

    public String money(double amount) {
        return "\u20b9 " + amount(amount);
    }

    public String amount(double amount) {
        NumberFormat fmt = NumberFormat.getNumberInstance(INDIAN_LOCALE);
        return fmt.format(amount);
    }

    private void seedDefaults() {
        put("APP", "window_title", "Taj Mahal \u2014 Command Center");

        put("HEADER", "brand_eyebrow", "TAJ MAHAL HOSPITALITY");
        put("HEADER", "brand_wordmark_expanded", "TAJ MAHAL");
        put("HEADER", "brand_wordmark_collapsed", "TAJ");
        put("HEADER", "brand_support", "Hospitality operations, refined to perfection");
        put("HEADER", "operator_role", "Administrator");
        put("HEADER", "operator_avatar", "AD");
        put("HEADER", "presence_status", "\u25cf Online");

        put("SIDEBAR", "section_label", "Operations");

        put("WORKSPACES", "rooms_title", "Rooms");
        put("WORKSPACES", "rooms_subtitle", "Curated inventory, pricing, and availability");
        put("WORKSPACES", "bookings_title", "Reservations");
        put("WORKSPACES", "bookings_subtitle", "Seamless guest arrival and stay management");
        put("WORKSPACES", "customers_title", "Guests");
        put("WORKSPACES", "customers_subtitle", "Guest profiles, preferences, and history");
        put("WORKSPACES", "billing_title", "Billing");
        put("WORKSPACES", "billing_subtitle", "Invoices, settlements, and departures");

        put("ROOMS", "section_add_new_room", "Introduce a New Room");
        put("ROOMS", "label_room_number", "Room Number");
        put("ROOMS", "prompt_room_number", "e.g. 105");
        put("ROOMS", "label_room_type", "Room Category");
        put("ROOMS", "label_price_per_day", "Rate per Night (\u20b9)");
        put("ROOMS", "prompt_price", "e.g. 4500");
        put("ROOMS", "button_add_room", "Add Room");
        put("ROOMS", "button_clear", "Clear");
        put("ROOMS", "section_room_inventory", "Room Inventory");
        put("ROOMS", "filter_showing_all", "Viewing: Entire Collection");
        put("ROOMS", "filter_showing_available", "Viewing: Available Only");
        put("ROOMS", "button_show_available_only", "Available Only");
        put("ROOMS", "button_show_all_rooms", "Entire Collection");
        put("ROOMS", "button_refresh", "Refresh");
        put("ROOMS", "table_placeholder", "No rooms available at the moment.");
        put("ROOMS", "table_col_room_no", "Room");
        put("ROOMS", "table_col_type", "Category");
        put("ROOMS", "table_col_price_per_day", "Rate / Night");
        put("ROOMS", "table_col_status", "Status");
        put("ROOMS", "table_col_action", "Actions");
        put("ROOMS", "status_available", "Available");
        put("ROOMS", "status_occupied", "Occupied");
        put("ROOMS", "button_delete", "Remove");
        put("ROOMS", "alert_validation_title", "Incomplete Details");
        put("ROOMS", "alert_validation_message", "Room number and nightly rate are required.");
        put("ROOMS", "alert_room_added_title", "Room Added");
        put("ROOMS", "alert_room_added_message", "Room {roomNumber} ({roomType}) has been successfully added.");
        put("ROOMS", "alert_invalid_price_title", "Invalid Rate");
        put("ROOMS", "alert_invalid_price_message", "Please enter a valid numeric value.");
        put("ROOMS", "alert_error_title", "Error");
        put("ROOMS", "alert_confirm_delete_title", "Confirm Removal");
        put("ROOMS", "alert_confirm_delete_message", "Remove Room {roomNumber}? This action cannot be undone.");
        put("ROOMS", "alert_deleted_title", "Room Removed");
        put("ROOMS", "alert_deleted_message", "Room {roomNumber} has been removed from inventory.");
        put("ROOMS", "alert_cannot_delete_title", "Action Restricted");

        put("BOOKINGS", "section_book_a_room", "Create Reservation");
        put("BOOKINGS", "label_customer_name", "Guest Name");
        put("BOOKINGS", "prompt_customer_name", "Enter full name");
        put("BOOKINGS", "label_contact", "Contact Number");
        put("BOOKINGS", "prompt_contact", "9876543210");
        put("BOOKINGS", "label_email", "Email Address");
        put("BOOKINGS", "prompt_email", "guest@email.com");
        put("BOOKINGS", "label_select_room", "Select Room");
        put("BOOKINGS", "prompt_select_room", "Choose from available rooms");
        put("BOOKINGS", "label_check_in_date", "Check-In");
        put("BOOKINGS", "label_check_out_date", "Check-Out");
        put("BOOKINGS", "button_book_room", "Confirm Reservation");
        put("BOOKINGS", "button_clear", "Clear");
        put("BOOKINGS", "button_refresh_rooms", "Refresh Availability");
        put("BOOKINGS", "section_active_bookings", "Current Stays");
        put("BOOKINGS", "table_placeholder", "No active stays at present.");
        put("BOOKINGS", "table_col_booking_id", "Reservation ID");
        put("BOOKINGS", "table_col_guest_name", "Guest");
        put("BOOKINGS", "table_col_contact", "Contact");
        put("BOOKINGS", "table_col_room", "Room");
        put("BOOKINGS", "table_col_dates", "Stay Duration");
        put("BOOKINGS", "table_col_total", "Total (\u20b9)");
        put("BOOKINGS", "menu_no_rooms_available", "No rooms currently available");
        put("BOOKINGS", "alert_validation_title", "Incomplete Details");
        put("BOOKINGS", "alert_validation_message", "Please select a room to proceed.");
        put("BOOKINGS", "alert_booking_successful_title", "Reservation Confirmed");
        put("BOOKINGS", "alert_booking_failed_title", "Reservation Unsuccessful");
        put("BOOKINGS", "summary_heading", "Reservation Summary");
        put("BOOKINGS", "summary_booking_id_label", "Reservation ID");
        put("BOOKINGS", "summary_guest_label", "Guest");
        put("BOOKINGS", "summary_room_label", "Room");
        put("BOOKINGS", "summary_check_in_label", "Check-In");
        put("BOOKINGS", "summary_check_out_label", "Check-Out");
        put("BOOKINGS", "summary_nights_label", "Nights");
        put("BOOKINGS", "summary_total_charge_label", "Total Amount");

        put("CUSTOMERS", "section_customer_and_booking_records", "Guest Registry");
        put("CUSTOMERS", "prompt_search", "Search by guest name or contact");
        put("CUSTOMERS", "button_search", "Search");
        put("CUSTOMERS", "button_show_all", "View All Guests");
        put("CUSTOMERS", "table_placeholder", "No records found.");
        put("CUSTOMERS", "table_col_booking_id", "Reservation ID");
        put("CUSTOMERS", "table_col_guest_name", "Guest");
        put("CUSTOMERS", "table_col_contact", "Contact");
        put("CUSTOMERS", "table_col_email", "Email");
        put("CUSTOMERS", "table_col_room_no", "Room");
        put("CUSTOMERS", "table_col_type", "Category");
        put("CUSTOMERS", "table_col_check_in", "Check-In");
        put("CUSTOMERS", "table_col_check_out", "Check-Out");
        put("CUSTOMERS", "table_col_nights", "Nights");
        put("CUSTOMERS", "table_col_total", "Total (\u20b9)");
        put("CUSTOMERS", "summary_total_active_bookings", "Active Stays: {count}");
        put("CUSTOMERS", "summary_total_revenue", "Total Revenue: \u20b9 {amount}");

        put("BILLING", "section_billing_and_checkout_management", "Billing & Departure");
        put("BILLING", "label_select_active_booking", "Select Reservation");
        put("BILLING", "prompt_choose_booking", "Choose a reservation to proceed");
        put("BILLING", "button_check_out_and_bill", "Complete Checkout & Generate Invoice");
        put("BILLING", "button_refresh", "Refresh");
        put("BILLING", "section_recent_invoices", "Recent Invoices");
        put("BILLING", "table_placeholder", "No invoices generated yet.");
        put("BILLING", "table_col_invoice_id", "Invoice ID");
        put("BILLING", "table_col_booking_ref", "Reservation Reference");
        put("BILLING", "table_col_amount", "Amount");
        put("BILLING", "table_col_date_generated", "Date");
        put("BILLING", "table_col_status", "Status");
        put("BILLING", "table_col_action", "Actions");
        put("BILLING", "button_mark_paid", "Mark as Settled");
        put("BILLING", "button_print_receipt", "Print Receipt");
        put("BILLING", "status_paid", "Settled");
        put("BILLING", "status_pending", "Pending");
        put("BILLING", "alert_no_booking_selected_title", "No Selection");
        put("BILLING", "alert_no_booking_selected_message", "Please select a reservation.");
        put("BILLING", "alert_checkout_complete_title", "Departure Completed");
        put("BILLING", "alert_checkout_complete_line_1", "Invoice generated: {billId}");
        put("BILLING", "alert_checkout_complete_line_2", "Total amount: \u20b9 {amount}");
        put("BILLING", "alert_checkout_complete_line_3", "Room is now ready for the next guest.");
        put("BILLING", "alert_print_unavailable_title", "Printer Unavailable");
        put("BILLING", "alert_print_unavailable_message", "No printer is available for receipt printing.");
        put("BILLING", "alert_receipt_failed_title", "Print Failed");
        put("BILLING", "alert_receipt_failed_message", "The receipt could not be sent to the printer.");
        put("BILLING", "alert_receipt_pending_title", "Payment Recorded");
        put("BILLING", "alert_receipt_pending_message", "Payment was recorded, but the receipt was not printed.");
        put("BILLING", "receipt_title", "Payment Receipt");
        put("BILLING", "receipt_invoice_id", "Invoice ID");
        put("BILLING", "receipt_booking_id", "Reservation ID");
        put("BILLING", "receipt_guest_name", "Guest");
        put("BILLING", "receipt_contact", "Contact");
        put("BILLING", "receipt_email", "Email");
        put("BILLING", "receipt_room", "Room");
        put("BILLING", "receipt_check_in", "Check-In");
        put("BILLING", "receipt_check_out", "Check-Out");
        put("BILLING", "receipt_nights", "Nights");
        put("BILLING", "receipt_payment_date", "Payment Date");
        put("BILLING", "receipt_status", "Status");
        put("BILLING", "receipt_total", "Total Paid");
        put("BILLING", "receipt_footer", "Thank you for choosing Taj Mahal hospitality.");
    }

    private void loadFromDatabase() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            seedDatabase(conn);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT sectionName, keyName, value FROM app_texts")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        values.put(composeKey(rs.getString("sectionName"), rs.getString("keyName")),
                                rs.getString("value"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[APP TEXT]" + e.getMessage());
        }
    }

    private void seedDatabase(Connection conn) throws SQLException {
        try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM app_texts");
             ResultSet rs = countStmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        try (PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO app_texts (sectionName, keyName, value) VALUES (?, ?, ?)")) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                String[] parts = entry.getKey().split("\\.", 2);
                insertStmt.setString(1, parts[0]);
                insertStmt.setString(2, parts[1]);
                insertStmt.setString(3, entry.getValue());
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
        }
    }

    private void put(String section, String key, String value) {
        values.put(composeKey(section, key), value);
    }

    private String composeKey(String section, String key) {
        return section + "." + key;
    }

    public static Map<String, String> tokens(String... values) {
        Map<String, String> tokens = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            tokens.put(values[i], values[i + 1]);
        }
        return tokens;
    }
}
