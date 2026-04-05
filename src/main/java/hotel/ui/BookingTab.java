package hotel.ui;

import hotel.model.*;
import hotel.service.BookingManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Booking Tab — allows staff to:
 *   - Book a room for a customer (with date pickers & validation)
 *   - View all active bookings in a table
 *   - Check out a guest (frees the room)
 */
public class BookingTab {

    private final BookingManager manager;
    private final Tab tab;

    // Form fields
    private TextField   tfName;
    private TextField   tfContact;
    private TextField   tfEmail;
    private MenuButton  btnRoomSelect;
    private Room        selectedRoom;
    private DatePicker  dpCheckIn;
    private DatePicker  dpCheckOut;

    // Table
    private TableView<Booking> tableView;

    public BookingTab(BookingManager manager) {
        this.manager = manager;
        ScrollPane sp = new ScrollPane(buildContent());
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        tab = new Tab("📋  Booking & Checkout", sp);
        tab.setClosable(false);
    }

    public Tab getTab() { return tab; }

    // ----------------------------------------------------------------
    //  UI Construction
    // ----------------------------------------------------------------

    private VBox buildContent() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(32));
        root.getStyleClass().add("tab-content");

        root.getChildren().addAll(
            buildSectionTitle("Book a Room"),
            buildBookingForm(),
            buildDivider(),
            buildSectionTitle("Active Bookings"),
            buildBookingTable()
        );
        return root;
    }

    private Label buildSectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("section-title");
        return lbl;
    }

    private Separator buildDivider() {
        Separator sep = new Separator();
        sep.setOpacity(0.3);
        return sep;
    }

    // ---- Booking Form ----

    private GridPane buildBookingForm() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(16);
        grid.getStyleClass().add("form-grid");

        // Row 0 — customer name
        grid.add(makeLabel("Customer Name:"), 0, 0);
        tfName = new TextField();
        tfName.setPromptText("Full name");
        tfName.getStyleClass().add("input-field");
        grid.add(tfName, 1, 0);

        // Contact
        grid.add(makeLabel("Contact (10 digits):"), 2, 0);
        tfContact = new TextField();
        tfContact.setPromptText("9876543210");
        tfContact.getStyleClass().add("input-field");
        grid.add(tfContact, 3, 0);

        // Row 1 — email and room
        grid.add(makeLabel("Email:"), 0, 1);
        tfEmail = new TextField();
        tfEmail.setPromptText("guest@email.com");
        tfEmail.getStyleClass().add("input-field");
        grid.add(tfEmail, 1, 1);

        grid.add(makeLabel("Select Room:"), 2, 1);
        btnRoomSelect = new MenuButton("Choose available room");
        btnRoomSelect.getStyleClass().addAll("input-field", "menu-button");
        btnRoomSelect.setPrefWidth(240);
        grid.add(btnRoomSelect, 3, 1);

        // Row 2 — Check In and Check Out
        grid.add(makeLabel("Check-In Date:"), 0, 2);
        dpCheckIn = new DatePicker(LocalDate.now());
        dpCheckIn.getStyleClass().add("input-field");
        grid.add(dpCheckIn, 1, 2);

        grid.add(makeLabel("Check-Out Date:"), 2, 2);
        dpCheckOut = new DatePicker(LocalDate.now().plusDays(1));
        dpCheckOut.getStyleClass().add("input-field");
        grid.add(dpCheckOut, 3, 2);

        // Row 3 — buttons
        HBox btnRow = new HBox(12);
        Button btnBook  = new Button("✅  Book Room");
        Button btnClear = new Button("✖  Clear");
        Button btnRefreshRooms = new Button("↻  Refresh Rooms");
        btnBook.getStyleClass().addAll("btn", "btn-primary");
        btnClear.getStyleClass().addAll("btn", "btn-secondary");
        btnRefreshRooms.getStyleClass().addAll("btn", "btn-info");

        btnBook.setOnAction(e -> handleBookRoom());
        btnClear.setOnAction(e -> clearForm());
        btnRefreshRooms.setOnAction(e -> refreshRoomMenu());

        btnRow.getChildren().addAll(btnBook, btnClear, btnRefreshRooms);
        grid.add(btnRow, 0, 3, 4, 1);

        // Populate menus on tab selection
        refreshRoomMenu();
        return grid;
    }

    // ---- Active Bookings Table ----

    @SuppressWarnings("unchecked")
    private TableView<Booking> buildBookingTable() {
        tableView = new TableView<>(manager.getBookings());
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(300);
        tableView.getStyleClass().add("data-table");
        tableView.setPlaceholder(new Label("No active bookings."));

        TableColumn<Booking, String> colId = new TableColumn<>("Booking ID");
        colId.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getBookingId()));

        TableColumn<Booking, String> colGuest = new TableColumn<>("Guest Name");
        colGuest.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCustomer().getName()));

        TableColumn<Booking, String> colContact = new TableColumn<>("Contact");
        colContact.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCustomer().getContactNumber()));

        TableColumn<Booking, String> colRoom = new TableColumn<>("Room");
        colRoom.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getRoom().getRoomNumber() + " (" +
                        c.getValue().getRoom().getRoomType() + ")"));

        TableColumn<Booking, String> colDates = new TableColumn<>("Check-In → Check-Out");
        colDates.setCellValueFactory(c -> {
            Booking b = c.getValue();
            return new SimpleStringProperty(
                    b.getCheckInDate() + " → " + b.getCheckOutDate() +
                    " (" + b.getNights() + " nights)");
        });

        TableColumn<Booking, String> colAmount = new TableColumn<>("Total (₹)");
        colAmount.setCellValueFactory(c -> {
            NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "IN"));
            return new SimpleStringProperty(
                    "₹ " + fmt.format(c.getValue().calculateTotalAmount()));
        });

        tableView.getColumns().addAll(
                colId, colGuest, colContact, colRoom, colDates, colAmount);
        return tableView;
    }

    // ----------------------------------------------------------------
    //  Event Handlers
    // ----------------------------------------------------------------

    private void handleBookRoom() {
        try {
            if (selectedRoom == null) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a room first.");
                return;
            }
            Booking booking = manager.bookRoom(
                    tfName.getText(),
                    tfContact.getText(),
                    tfEmail.getText(),
                    selectedRoom,
                    dpCheckIn.getValue(),
                    dpCheckOut.getValue()
            );
            refreshRoomMenu();
            clearForm();

            // Success summary dialog
            String summary = String.format(
                "✅ Booking Confirmed!\n\n" +
                "Booking ID  : %s\n" +
                "Guest       : %s\n" +
                "Room        : %s (%s)\n" +
                "Check-In    : %s\n" +
                "Check-Out   : %s\n" +
                "Nights      : %d\n" +
                "Total Charge: ₹ %.2f",
                booking.getBookingId(),
                booking.getCustomer().getName(),
                booking.getRoom().getRoomNumber(),
                booking.getRoom().getRoomType(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNights(),
                booking.calculateTotalAmount()
            );
            showAlert(Alert.AlertType.INFORMATION, "Booking Successful", summary);

        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.ERROR, "Booking Failed", ex.getMessage());
        }
    }



    // ----------------------------------------------------------------
    //  Helpers
    // ----------------------------------------------------------------

    /** Refresh the available rooms and categories. */
    public void refreshRoomMenu() {
        btnRoomSelect.getItems().clear();

        // Find unique categories
        for (RoomType type : RoomType.values()) {
            java.util.List<Room> roomsOfType = manager.getAvailableRooms().stream()
                .filter(r -> r.getRoomType() == type)
                .toList();

            if (!roomsOfType.isEmpty()) {
                Menu categoryMenu = new Menu(type.getDisplayName() + " (" + roomsOfType.size() + ")");
                for (Room r : roomsOfType) {
                    MenuItem item = new MenuItem("Room " + r.getRoomNumber() + " - ₹" + r.getPricePerDay());
                    item.setOnAction(e -> {
                        selectedRoom = r;
                        btnRoomSelect.setText("Room " + r.getRoomNumber() + " (" + type.getDisplayName() + ")");
                    });
                    categoryMenu.getItems().add(item);
                }
                btnRoomSelect.getItems().add(categoryMenu);
            }
        }
        
        if (btnRoomSelect.getItems().isEmpty()) {
            MenuItem noRooms = new MenuItem("No rooms available");
            noRooms.setDisable(true);
            btnRoomSelect.getItems().add(noRooms);
        }
        
        if (selectedRoom != null && !manager.getAvailableRooms().contains(selectedRoom)) {
            selectedRoom = null;
            btnRoomSelect.setText("Choose available room");
        }
    }

    private void clearForm() {
        tfName.clear();
        tfContact.clear();
        tfEmail.clear();
        selectedRoom = null;
        btnRoomSelect.setText("Choose available room");
        dpCheckIn.setValue(LocalDate.now());
        dpCheckOut.setValue(LocalDate.now().plusDays(1));
    }

    private Label makeLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("form-label");
        return lbl;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
