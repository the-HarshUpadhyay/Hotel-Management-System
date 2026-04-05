package hotel.ui;

import hotel.config.AppText;
import hotel.model.Booking;
import hotel.service.BookingManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

public class CustomerTab {

    private final BookingManager manager;
    private final Tab tab;
    private final AppText text = AppText.get();

    private TableView<Booking> tableView;
    private TextField tfSearch;

    public CustomerTab(BookingManager manager) {
        this.manager = manager;
        ScrollPane sp = new ScrollPane(buildContent());
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        tab = new Tab(text.text("WORKSPACES", "customers_title", "Customers"), sp);
        tab.setClosable(false);
    }

    public Tab getTab() {
        return tab;
    }

    private VBox buildContent() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(32));
        root.getStyleClass().add("tab-content");

        root.getChildren().addAll(
                buildSectionTitle(text.text("CUSTOMERS", "section_customer_and_booking_records", "Customer and Booking Records")),
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

    private HBox buildSearchBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        tfSearch = new TextField();
        tfSearch.setPromptText(text.text("CUSTOMERS", "prompt_search", "Search by guest name or contact"));
        tfSearch.getStyleClass().add("input-field");
        tfSearch.setPrefWidth(350);

        Button btnSearch = new Button(text.text("CUSTOMERS", "button_search", "Search"));
        Button btnReset = new Button(text.text("CUSTOMERS", "button_show_all", "Show All"));
        btnSearch.getStyleClass().addAll("btn", "btn-primary");
        btnReset.getStyleClass().addAll("btn", "btn-secondary");

        btnSearch.setOnAction(e -> applyFilter());
        btnReset.setOnAction(e -> {
            tfSearch.clear();
            applyFilter();
        });

        tfSearch.textProperty().addListener((obs, old, val) -> applyFilter());

        bar.getChildren().addAll(tfSearch, btnSearch, btnReset);
        return bar;
    }

    @SuppressWarnings("unchecked")
    private TableView<Booking> buildRecordsTable() {
        tableView = new TableView<>(manager.getBookings());
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(380);
        tableView.getStyleClass().add("data-table");
        tableView.setPlaceholder(new Label(text.text("CUSTOMERS", "table_placeholder", "No records found.")));

        TableColumn<Booking, String> colId = new TableColumn<>(text.text("CUSTOMERS", "table_col_booking_id", "Booking ID"));
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookingId()));
        colId.setMinWidth(90);

        TableColumn<Booking, String> colName = new TableColumn<>(text.text("CUSTOMERS", "table_col_guest_name", "Guest Name"));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomer().getName()));

        TableColumn<Booking, String> colContact = new TableColumn<>(text.text("CUSTOMERS", "table_col_contact", "Contact"));
        colContact.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomer().getContactNumber()));

        TableColumn<Booking, String> colEmail = new TableColumn<>(text.text("CUSTOMERS", "table_col_email", "Email"));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomer().getEmail()));

        TableColumn<Booking, String> colRoom = new TableColumn<>(text.text("CUSTOMERS", "table_col_room_no", "Room No."));
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoom().getRoomNumber()));
        colRoom.setMaxWidth(90);

        TableColumn<Booking, String> colType = new TableColumn<>(text.text("CUSTOMERS", "table_col_type", "Type"));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoom().getRoomType().getDisplayName()));
        colType.setMaxWidth(80);

        TableColumn<Booking, String> colCheckIn = new TableColumn<>(text.text("CUSTOMERS", "table_col_check_in", "Check-In"));
        colCheckIn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckInDate().toString()));

        TableColumn<Booking, String> colCheckOut = new TableColumn<>(text.text("CUSTOMERS", "table_col_check_out", "Check-Out"));
        colCheckOut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckOutDate().toString()));

        TableColumn<Booking, String> colNights = new TableColumn<>(text.text("CUSTOMERS", "table_col_nights", "Nights"));
        colNights.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getNights())));
        colNights.setMaxWidth(60);

        TableColumn<Booking, String> colAmount = new TableColumn<>(text.text("CUSTOMERS", "table_col_total", "Total (Rs)"));
        colAmount.setCellValueFactory(c -> {
            return new SimpleStringProperty(text.money(c.getValue().calculateTotalAmount()));
        });

        tableView.getColumns().addAll(
                colId, colName, colContact, colEmail,
                colRoom, colType, colCheckIn, colCheckOut, colNights, colAmount);
        return tableView;
    }

    private HBox buildSummaryBar() {
        HBox bar = new HBox(30);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("summary-bar");
        bar.setPadding(new Insets(14, 20, 14, 20));

        Label lblTotal = new Label();
        Label lblRevenue = new Label();

        manager.getBookings().addListener((ListChangeListener<Booking>) change -> updateSummary(lblTotal, lblRevenue));
        updateSummary(lblTotal, lblRevenue);

        bar.getChildren().addAll(lblTotal, lblRevenue);
        return bar;
    }

    private void applyFilter() {
        String query = tfSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            tableView.setItems(manager.getBookings());
        } else {
            var filtered = manager.getBookings().stream()
                    .filter(b -> b.getCustomer().getName().toLowerCase().contains(query)
                            || b.getCustomer().getContactNumber().contains(query))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            tableView.setItems(filtered);
        }
    }

    private void updateSummary(Label lblTotal, Label lblRevenue) {
        int count = manager.getBookings().size();
        double revenue = manager.getBookings().stream()
                .mapToDouble(Booking::calculateTotalAmount)
                .sum();
        lblTotal.setText(text.format("CUSTOMERS", "summary_total_active_bookings",
                "Total Active Bookings: {count}", AppText.tokens("count", String.valueOf(count))));
        lblTotal.getStyleClass().add("summary-label");

        lblRevenue.setText(text.format("CUSTOMERS", "summary_total_revenue",
                "Total Revenue: \u20b9 {amount}",
                AppText.tokens("amount", text.amount(revenue))));
        lblRevenue.getStyleClass().add("summary-label");
    }
}
