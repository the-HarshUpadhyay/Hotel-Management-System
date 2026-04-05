package hotel.ui;

import hotel.model.Booking;
import hotel.service.BookingManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Customer Records Tab — provides a read-only view of all bookings
 * with a name-based search filter.
 *
 * Demonstrates event-driven filtering with a live TextField listener.
 */
public class CustomerTab {

    private final BookingManager manager;
    private final Tab tab;

    private TableView<Booking> tableView;
    private TextField tfSearch;

    public CustomerTab(BookingManager manager) {
        this.manager = manager;
        ScrollPane sp = new ScrollPane(buildContent());
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        tab = new Tab("👤  Customer Records", sp);
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
            buildSectionTitle("Customer & Booking Records"),
            buildSearchBar(),
            buildRecordsTable(),
            buildSummaryBar()
        );
        return root;
    }

    private Label buildSectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("section-title");
        return lbl;
    }

    // ---- Search Bar ----

    private HBox buildSearchBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        tfSearch = new TextField();
        tfSearch.setPromptText("🔍  Search by guest name or contact…");
        tfSearch.getStyleClass().add("input-field");
        tfSearch.setPrefWidth(350);

        Button btnSearch = new Button("Search");
        Button btnReset  = new Button("Show All");
        btnSearch.getStyleClass().addAll("btn", "btn-primary");
        btnReset.getStyleClass().addAll("btn", "btn-secondary");

        btnSearch.setOnAction(e -> applyFilter());
        btnReset.setOnAction(e -> { tfSearch.clear(); applyFilter(); });

        // Live filter as user types
        tfSearch.textProperty().addListener((obs, old, val) -> applyFilter());

        bar.getChildren().addAll(tfSearch, btnSearch, btnReset);
        return bar;
    }

    // ---- Records Table ----

    @SuppressWarnings("unchecked")
    private TableView<Booking> buildRecordsTable() {
        tableView = new TableView<>(manager.getBookings());
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(380);
        tableView.getStyleClass().add("data-table");
        tableView.setPlaceholder(new Label("No records found."));

        TableColumn<Booking, String> colId = new TableColumn<>("Booking ID");
        colId.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getBookingId()));
        colId.setMinWidth(90);

        TableColumn<Booking, String> colName = new TableColumn<>("Guest Name");
        colName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCustomer().getName()));

        TableColumn<Booking, String> colContact = new TableColumn<>("Contact");
        colContact.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCustomer().getContactNumber()));

        TableColumn<Booking, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCustomer().getEmail()));

        TableColumn<Booking, String> colRoom = new TableColumn<>("Room No.");
        colRoom.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getRoom().getRoomNumber()));
        colRoom.setMaxWidth(90);

        TableColumn<Booking, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getRoom().getRoomType().getDisplayName()));
        colType.setMaxWidth(80);

        TableColumn<Booking, String> colCheckIn = new TableColumn<>("Check-In");
        colCheckIn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCheckInDate().toString()));

        TableColumn<Booking, String> colCheckOut = new TableColumn<>("Check-Out");
        colCheckOut.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCheckOutDate().toString()));

        TableColumn<Booking, String> colNights = new TableColumn<>("Nights");
        colNights.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getNights())));
        colNights.setMaxWidth(60);

        TableColumn<Booking, String> colAmount = new TableColumn<>("Total (₹)");
        colAmount.setCellValueFactory(c -> {
            NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "IN"));
            return new SimpleStringProperty(
                    "₹ " + fmt.format(c.getValue().calculateTotalAmount()));
        });

        tableView.getColumns().addAll(
                colId, colName, colContact, colEmail,
                colRoom, colType, colCheckIn, colCheckOut, colNights, colAmount);
        return tableView;
    }

    // ---- Summary Bar ----

    private HBox buildSummaryBar() {
        HBox bar = new HBox(30);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("summary-bar");
        bar.setPadding(new Insets(14, 20, 14, 20));

        Label lblTotal   = new Label();
        Label lblRevenue = new Label();

        // Update summary whenever bookings change
        manager.getBookings().addListener(
            (javafx.collections.ListChangeListener<Booking>) change -> updateSummary(lblTotal, lblRevenue));
        updateSummary(lblTotal, lblRevenue);

        bar.getChildren().addAll(lblTotal, lblRevenue);
        return bar;
    }

    // ----------------------------------------------------------------
    //  Helpers
    // ----------------------------------------------------------------

    /** Filter table rows by search text. */
    private void applyFilter() {
        String query = tfSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            tableView.setItems(manager.getBookings());
        } else {
            var filtered = manager.getBookings().stream()
                    .filter(b -> b.getCustomer().getName().toLowerCase().contains(query) ||
                                 b.getCustomer().getContactNumber().contains(query))
                    .collect(Collectors.toCollection(
                            javafx.collections.FXCollections::observableArrayList));
            tableView.setItems(filtered);
        }
    }

    /** Updates the bottom stats bar. */
    private void updateSummary(Label lblTotal, Label lblRevenue) {
        int count = manager.getBookings().size();
        double revenue = manager.getBookings().stream()
                .mapToDouble(Booking::calculateTotalAmount).sum();
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "IN"));

        lblTotal.setText("📊 Total Active Bookings: " + count);
        lblTotal.getStyleClass().add("summary-label");

        lblRevenue.setText("💰 Total Revenue: ₹ " + fmt.format(revenue));
        lblRevenue.getStyleClass().add("summary-label");
    }
}
