package hotel.ui;

import hotel.model.*;
import hotel.service.BookingManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Room Management Tab — lets staff:
 *   - Add new rooms (number, type, price)
 *   - View all rooms in a TableView
 *   - Filter to show only available rooms
 *   - Delete available rooms
 */
public class RoomTab {

    private final BookingManager manager;
    private final Tab tab;

    // Form controls
    private TextField   tfRoomNumber;
    private ComboBox<RoomType> cbRoomType;
    private TextField   tfPrice;

    // Table
    private TableView<Room> tableView;
    private boolean showingAvailableOnly = false;

    public RoomTab(BookingManager manager) {
        this.manager = manager;
        ScrollPane sp = new ScrollPane(buildContent());
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        tab = new Tab("🏨  Room Management", sp);
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
            buildSectionTitle("Add New Room"),
            buildAddRoomForm(),
            buildDivider(),
            buildSectionTitle("Room Inventory"),
            buildFilterBar(),
            buildRoomTable()
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

    // ---- Add Room Form ----

    private GridPane buildAddRoomForm() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(16);
        grid.getStyleClass().add("form-grid");

        // Room Number
        grid.add(makeLabel("Room Number:"), 0, 0);
        tfRoomNumber = new TextField();
        tfRoomNumber.setPromptText("e.g. 105");
        tfRoomNumber.getStyleClass().add("input-field");
        grid.add(tfRoomNumber, 1, 0);

        // Room Type
        grid.add(makeLabel("Room Type:"), 2, 0);
        cbRoomType = new ComboBox<>(
                FXCollections.observableArrayList(RoomType.values()));
        cbRoomType.setValue(RoomType.SINGLE);
        cbRoomType.getStyleClass().add("input-field");
        cbRoomType.setPrefWidth(140);
        grid.add(cbRoomType, 3, 0);

        // Price
        grid.add(makeLabel("Price / Day (₹):"), 0, 1);
        tfPrice = new TextField();
        tfPrice.setPromptText("e.g. 1200");
        tfPrice.getStyleClass().add("input-field");
        grid.add(tfPrice, 1, 1);

        // Buttons
        HBox btnRow = new HBox(12);
        Button btnAdd   = new Button("➕  Add Room");
        Button btnClear = new Button("✖  Clear");
        btnAdd.getStyleClass().addAll("btn", "btn-primary");
        btnClear.getStyleClass().addAll("btn", "btn-secondary");
        btnAdd.setOnAction(e -> handleAddRoom());
        btnClear.setOnAction(e -> clearForm());
        btnRow.getChildren().addAll(btnAdd, btnClear);
        grid.add(btnRow, 0, 2, 4, 1);

        return grid;
    }

    // ---- Filter Bar ----

    private HBox buildFilterBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label lblStatus = new Label("Showing: All Rooms");
        lblStatus.getStyleClass().add("filter-label");
        lblStatus.setMinWidth(200);

        Button btnToggle = new Button("🔍  Show Available Only");
        btnToggle.getStyleClass().addAll("btn", "btn-info");
        btnToggle.setOnAction(e -> {
            showingAvailableOnly = !showingAvailableOnly;
            refreshTable();
            if (showingAvailableOnly) {
                btnToggle.setText("📋  Show All Rooms");
                lblStatus.setText("Showing: Available Rooms");
            } else {
                btnToggle.setText("🔍  Show Available Only");
                lblStatus.setText("Showing: All Rooms");
            }
        });

        Button btnRefresh = new Button("↻  Refresh");
        btnRefresh.getStyleClass().addAll("btn", "btn-secondary");
        btnRefresh.setOnAction(e -> refreshTable());

        bar.getChildren().addAll(btnToggle, btnRefresh, lblStatus);
        return bar;
    }

    // ---- Room Table ----

    @SuppressWarnings("unchecked")
    private TableView<Room> buildRoomTable() {
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(340);
        tableView.getStyleClass().add("data-table");
        tableView.setPlaceholder(new Label("No rooms found."));

        TableColumn<Room, String> colNum  = new TableColumn<>("Room No.");
        colNum.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Room, RoomType> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("roomType"));

        TableColumn<Room, Double> colPrice = new TableColumn<>("Price / Day");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerDay"));
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); }
                else { setText("₹ " + NumberFormat.getNumberInstance(new Locale("en", "IN")).format(val)); }
            }
        });

        TableColumn<Room, Boolean> colAvail = new TableColumn<>("Status");
        colAvail.setCellValueFactory(new PropertyValueFactory<>("available"));
        colAvail.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Boolean avail, boolean empty) {
                super.updateItem(avail, empty);
                if (empty || avail == null) { setText(null); setStyle(""); return; }
                setText(avail ? "✅  Available" : "🔴  Occupied");
                setStyle(avail ? "-fx-text-fill: #22c55e; -fx-font-weight: bold;"
                               : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            }
        });

        // Delete action column
        TableColumn<Room, Void> colAction = new TableColumn<>("Action");
        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button btnDel = new Button("🗑 Delete");
            {
                btnDel.getStyleClass().addAll("btn", "btn-danger-sm");
                btnDel.setOnAction(e -> {
                    Room room = getTableView().getItems().get(getIndex());
                    handleDeleteRoom(room);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btnDel);
            }
        });

        tableView.getColumns().addAll(colNum, colType, colPrice, colAvail, colAction);
        tableView.setItems(manager.getRooms());
        return tableView;
    }

    // ----------------------------------------------------------------
    //  Event Handlers
    // ----------------------------------------------------------------

    private void handleAddRoom() {
        try {
            String num    = tfRoomNumber.getText().trim();
            RoomType type = cbRoomType.getValue();
            String priceText = tfPrice.getText().trim();

            if (num.isEmpty() || priceText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error",
                        "Room Number and Price are required fields.");
                return;
            }

            double price = Double.parseDouble(priceText);
            manager.addRoom(num, type, price);
            refreshTable();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Room Added",
                    "Room " + num + " (" + type + ") added successfully.");

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Price",
                    "Please enter a valid numeric price.");
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }

    private void handleDeleteRoom(Room room) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete Room " + room.getRoomNumber() + "?\nThis cannot be undone.",
                ButtonType.YES, ButtonType.CANCEL);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    manager.deleteRoom(room);
                    refreshTable();
                    showAlert(Alert.AlertType.INFORMATION, "Deleted",
                            "Room " + room.getRoomNumber() + " deleted.");
                } catch (IllegalArgumentException ex) {
                    showAlert(Alert.AlertType.ERROR, "Cannot Delete", ex.getMessage());
                }
            }
        });
    }

    // ----------------------------------------------------------------
    //  Helpers
    // ----------------------------------------------------------------

    private void refreshTable() {
        if (showingAvailableOnly) {
            tableView.setItems(manager.getAvailableRooms());
        } else {
            tableView.setItems(manager.getRooms());
        }
        tableView.refresh();
    }

    private void clearForm() {
        tfRoomNumber.clear();
        tfPrice.clear();
        cbRoomType.setValue(RoomType.SINGLE);
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
