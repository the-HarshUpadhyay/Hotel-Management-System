package hotel.ui;

import hotel.config.AppText;
import hotel.model.Room;
import hotel.model.RoomType;
import hotel.service.BookingManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RoomTab {

    private final BookingManager manager;
    private final Tab tab;
    private final AppText text = AppText.get();

    private TextField tfRoomNumber;
    private ComboBox<RoomType> cbRoomType;
    private TextField tfPrice;
    private TableView<Room> tableView;
    private boolean showingAvailableOnly = false;
    private Label lblStatus;
    private Button btnToggle;

    public RoomTab(BookingManager manager) {
        this.manager = manager;
        ScrollPane sp = new ScrollPane(buildContent());
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        tab = new Tab(text.text("WORKSPACES", "rooms_title", "Rooms"), sp);
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
                buildSectionTitle(text.text("ROOMS", "section_add_new_room", "Add New Room")),
                buildAddRoomForm(),
                buildDivider(),
                buildSectionTitle(text.text("ROOMS", "section_room_inventory", "Room Inventory")),
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

    private GridPane buildAddRoomForm() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(16);
        grid.getStyleClass().add("form-grid");

        grid.add(makeLabel(text.text("ROOMS", "label_room_number", "Room Number:")), 0, 0);
        tfRoomNumber = new TextField();
        tfRoomNumber.setPromptText(text.text("ROOMS", "prompt_room_number", "e.g. 105"));
        tfRoomNumber.getStyleClass().add("input-field");
        grid.add(tfRoomNumber, 1, 0);

        grid.add(makeLabel(text.text("ROOMS", "label_room_type", "Room Type:")), 2, 0);
        cbRoomType = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
        cbRoomType.setValue(RoomType.SINGLE);
        cbRoomType.setVisibleRowCount(RoomType.values().length);
        cbRoomType.setPrefWidth(220);
        cbRoomType.getStyleClass().add("input-field");
        cbRoomType.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(RoomType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
        cbRoomType.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(RoomType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
        grid.add(cbRoomType, 3, 0);

        grid.add(makeLabel(text.text("ROOMS", "label_price_per_day", "Price / Day (Rs):")), 0, 1);
        tfPrice = new TextField();
        tfPrice.setPromptText(text.text("ROOMS", "prompt_price", "e.g. 1200"));
        tfPrice.getStyleClass().add("input-field");
        grid.add(tfPrice, 1, 1);

        HBox btnRow = new HBox(12);
        Button btnAdd = new Button(text.text("ROOMS", "button_add_room", "+ Add Room"));
        Button btnClear = new Button(text.text("ROOMS", "button_clear", "Clear"));
        btnAdd.getStyleClass().addAll("btn", "btn-primary");
        btnClear.getStyleClass().addAll("btn", "btn-secondary");
        btnAdd.setOnAction(e -> handleAddRoom());
        btnClear.setOnAction(e -> clearForm());
        btnRow.getChildren().addAll(btnAdd, btnClear);
        grid.add(btnRow, 0, 2, 4, 1);

        return grid;
    }

    private HBox buildFilterBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);

        lblStatus = new Label(text.text("ROOMS", "filter_showing_all", "Showing: All Rooms"));
        lblStatus.getStyleClass().add("filter-label");
        lblStatus.setMinWidth(200);

        btnToggle = new Button(text.text("ROOMS", "button_show_available_only", "Show Available Only"));
        btnToggle.getStyleClass().addAll("btn", "btn-info");
        btnToggle.setOnAction(e -> {
            showingAvailableOnly = !showingAvailableOnly;
            refreshTable();
        });

        Button btnRefresh = new Button(text.text("ROOMS", "button_refresh", "Refresh"));
        btnRefresh.getStyleClass().addAll("btn", "btn-secondary");
        btnRefresh.setOnAction(e -> refreshTable());

        bar.getChildren().addAll(btnToggle, btnRefresh, lblStatus);
        return bar;
    }

    @SuppressWarnings("unchecked")
    private TableView<Room> buildRoomTable() {
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(340);
        tableView.getStyleClass().add("data-table");
        tableView.setPlaceholder(new Label(text.text("ROOMS", "table_placeholder", "No rooms found.")));

        TableColumn<Room, String> colNum = new TableColumn<>(text.text("ROOMS", "table_col_room_no", "Room No."));
        colNum.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Room, RoomType> colType = new TableColumn<>(text.text("ROOMS", "table_col_type", "Type"));
        colType.setCellValueFactory(new PropertyValueFactory<>("roomType"));

        TableColumn<Room, Double> colPrice = new TableColumn<>(text.text("ROOMS", "table_col_price_per_day", "Price / Day"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerDay"));
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                } else {
                    setText(text.money(val));
                }
            }
        });

        TableColumn<Room, Boolean> colAvail = new TableColumn<>(text.text("ROOMS", "table_col_status", "Status"));
        colAvail.setCellValueFactory(new PropertyValueFactory<>("available"));
        colAvail.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean avail, boolean empty) {
                super.updateItem(avail, empty);
                if (empty || avail == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(avail
                        ? text.text("ROOMS", "status_available", "Available")
                        : text.text("ROOMS", "status_occupied", "Occupied"));
                setStyle(avail
                        ? "-fx-text-fill: #22c55e; -fx-font-weight: bold;"
                        : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            }
        });

        TableColumn<Room, Void> colAction = new TableColumn<>(text.text("ROOMS", "table_col_action", "Action"));
        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button btnDel = new Button(text.text("ROOMS", "button_delete", "Delete"));

            {
                btnDel.getStyleClass().addAll("btn", "btn-danger-sm");
                btnDel.setOnAction(e -> {
                    Room room = getTableView().getItems().get(getIndex());
                    handleDeleteRoom(room);
                });
            }

            @Override
            protected void updateItem(Void value, boolean empty) {
                super.updateItem(value, empty);
                setGraphic(empty ? null : btnDel);
            }
        });

        tableView.getColumns().addAll(colNum, colType, colPrice, colAvail, colAction);
        tableView.setItems(manager.getRooms());
        return tableView;
    }

    private void handleAddRoom() {
        try {
            String num = tfRoomNumber.getText().trim();
            RoomType type = cbRoomType.getValue();
            String priceText = tfPrice.getText().trim();

            if (num.isEmpty() || priceText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING,
                        text.text("ROOMS", "alert_validation_title", "Validation Error"),
                        text.text("ROOMS", "alert_validation_message", "Room Number and Price are required fields."));
                return;
            }

            double price = Double.parseDouble(priceText);
            manager.addRoom(num, type, price);
            refreshTable();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION,
                    text.text("ROOMS", "alert_room_added_title", "Room Added"),
                    text.format("ROOMS", "alert_room_added_message",
                            "Room {roomNumber} ({roomType}) added successfully.",
                            AppText.tokens("roomNumber", num, "roomType", type.getDisplayName())));
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR,
                    text.text("ROOMS", "alert_invalid_price_title", "Invalid Price"),
                    text.text("ROOMS", "alert_invalid_price_message", "Please enter a valid numeric price."));
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.ERROR, text.text("ROOMS", "alert_error_title", "Error"), ex.getMessage());
        }
    }

    private void handleDeleteRoom(Room room) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                text.format("ROOMS", "alert_confirm_delete_message",
                        "Delete Room {roomNumber}? This cannot be undone.",
                        AppText.tokens("roomNumber", room.getRoomNumber())).replace("? ", "?\n"),
                ButtonType.YES, ButtonType.CANCEL);
        confirm.setTitle(text.text("ROOMS", "alert_confirm_delete_title", "Confirm Delete"));
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    manager.deleteRoom(room);
                    refreshTable();
                    showAlert(Alert.AlertType.INFORMATION,
                            text.text("ROOMS", "alert_deleted_title", "Deleted"),
                            text.format("ROOMS", "alert_deleted_message", "Room {roomNumber} deleted.",
                                    AppText.tokens("roomNumber", room.getRoomNumber())));
                } catch (IllegalArgumentException ex) {
                    showAlert(Alert.AlertType.ERROR,
                            text.text("ROOMS", "alert_cannot_delete_title", "Cannot Delete"),
                            ex.getMessage());
                }
            }
        });
    }

    private void refreshTable() {
        if (showingAvailableOnly) {
            tableView.setItems(manager.getAvailableRooms());
            btnToggle.setText(text.text("ROOMS", "button_show_all_rooms", "Show All Rooms"));
            lblStatus.setText(text.text("ROOMS", "filter_showing_available", "Showing: Available Rooms"));
        } else {
            tableView.setItems(manager.getRooms());
            btnToggle.setText(text.text("ROOMS", "button_show_available_only", "Show Available Only"));
            lblStatus.setText(text.text("ROOMS", "filter_showing_all", "Showing: All Rooms"));
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
