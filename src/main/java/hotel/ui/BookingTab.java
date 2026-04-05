package hotel.ui;

import hotel.config.AppText;
import hotel.model.Booking;
import hotel.model.Room;
import hotel.model.RoomType;
import hotel.service.BookingManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.DateCell;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
public class BookingTab {

    private static final PseudoClass FOCUSED = PseudoClass.getPseudoClass("focused");

    private final BookingManager manager;
    private final Tab tab;
    private final AppText text = AppText.get();

    private TextField tfName;
    private TextField tfContact;
    private TextField tfEmail;
    private MenuButton btnRoomSelect;
    private Room selectedRoom;
    private DatePicker dpCheckIn;
    private DatePicker dpCheckOut;
    private TableView<Booking> tableView;

    public BookingTab(BookingManager manager) {
        this.manager = manager;
        ScrollPane sp = new ScrollPane(buildContent());
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        tab = new Tab(text.text("WORKSPACES", "bookings_title", "Bookings"), sp);
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
                buildSectionTitle(text.text("BOOKINGS", "section_book_a_room", "Book a Room")),
                buildBookingForm(),
                buildDivider(),
                buildSectionTitle(text.text("BOOKINGS", "section_active_bookings", "Active Bookings")),
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

    private GridPane buildBookingForm() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(16);
        grid.getStyleClass().add("form-grid");

        grid.add(makeLabel(text.text("BOOKINGS", "label_customer_name", "Customer Name:")), 0, 0);
        tfName = new TextField();
        tfName.setPromptText(text.text("BOOKINGS", "prompt_customer_name", "Full name"));
        tfName.getStyleClass().add("input-field");
        grid.add(tfName, 1, 0);

        grid.add(makeLabel(text.text("BOOKINGS", "label_contact", "Contact (10 digits):")), 2, 0);
        tfContact = new TextField();
        tfContact.setPromptText(text.text("BOOKINGS", "prompt_contact", "9876543210"));
        tfContact.getStyleClass().add("input-field");
        grid.add(tfContact, 3, 0);

        grid.add(makeLabel(text.text("BOOKINGS", "label_email", "Email:")), 0, 1);
        tfEmail = new TextField();
        tfEmail.setPromptText(text.text("BOOKINGS", "prompt_email", "guest@email.com"));
        tfEmail.getStyleClass().add("input-field");
        grid.add(tfEmail, 1, 1);

        grid.add(makeLabel(text.text("BOOKINGS", "label_select_room", "Select Room:")), 2, 1);
        btnRoomSelect = new MenuButton(text.text("BOOKINGS", "prompt_select_room", "Choose available room"));
        btnRoomSelect.getStyleClass().addAll("input-field", "menu-button");
        btnRoomSelect.setPrefWidth(240);
        grid.add(btnRoomSelect, 3, 1);

        grid.add(makeLabel(text.text("BOOKINGS", "label_check_in_date", "Check-In Date:")), 0, 2);
        dpCheckIn = new DatePicker(LocalDate.now());
        configureDatePicker(dpCheckIn, true);
        grid.add(buildLuxuryDateField(dpCheckIn), 1, 2);

        grid.add(makeLabel(text.text("BOOKINGS", "label_check_out_date", "Check-Out Date:")), 2, 2);
        dpCheckOut = new DatePicker(LocalDate.now().plusDays(1));
        configureDatePicker(dpCheckOut, false);
        grid.add(buildLuxuryDateField(dpCheckOut), 3, 2);

        HBox btnRow = new HBox(12);
        Button btnBook = new Button(text.text("BOOKINGS", "button_book_room", "Book Room"));
        Button btnClear = new Button(text.text("BOOKINGS", "button_clear", "Clear"));
        Button btnRefreshRooms = new Button(text.text("BOOKINGS", "button_refresh_rooms", "Refresh Rooms"));
        btnBook.getStyleClass().addAll("btn", "btn-primary");
        btnClear.getStyleClass().addAll("btn", "btn-secondary");
        btnRefreshRooms.getStyleClass().addAll("btn", "btn-info");

        btnBook.setOnAction(e -> handleBookRoom());
        btnClear.setOnAction(e -> clearForm());
        btnRefreshRooms.setOnAction(e -> refreshRoomMenu());

        btnRow.getChildren().addAll(btnBook, btnClear, btnRefreshRooms);
        grid.add(btnRow, 0, 3, 4, 1);

        refreshRoomMenu();
        return grid;
    }

    @SuppressWarnings("unchecked")
    private TableView<Booking> buildBookingTable() {
        tableView = new TableView<>(manager.getBookings());
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(300);
        tableView.getStyleClass().add("data-table");
        tableView.setPlaceholder(new Label(text.text("BOOKINGS", "table_placeholder", "No active bookings.")));

        TableColumn<Booking, String> colId = new TableColumn<>(text.text("BOOKINGS", "table_col_booking_id", "Booking ID"));
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookingId()));

        TableColumn<Booking, String> colGuest = new TableColumn<>(text.text("BOOKINGS", "table_col_guest_name", "Guest Name"));
        colGuest.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomer().getName()));

        TableColumn<Booking, String> colContact = new TableColumn<>(text.text("BOOKINGS", "table_col_contact", "Contact"));
        colContact.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomer().getContactNumber()));

        TableColumn<Booking, String> colRoom = new TableColumn<>(text.text("BOOKINGS", "table_col_room", "Room"));
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRoom().getRoomNumber() + " (" + c.getValue().getRoom().getRoomType() + ")"));

        TableColumn<Booking, String> colDates = new TableColumn<>(text.text("BOOKINGS", "table_col_dates", "Check-In to Check-Out"));
        colDates.setCellValueFactory(c -> {
            Booking b = c.getValue();
            return new SimpleStringProperty(
                    b.getCheckInDate() + " to " + b.getCheckOutDate() + " (" + b.getNights() + " nights)");
        });

        TableColumn<Booking, String> colAmount = new TableColumn<>(text.text("BOOKINGS", "table_col_total", "Total (Rs)"));
        colAmount.setCellValueFactory(c -> {
            return new SimpleStringProperty(text.money(c.getValue().calculateTotalAmount()));
        });

        tableView.getColumns().addAll(colId, colGuest, colContact, colRoom, colDates, colAmount);
        return tableView;
    }

    private void handleBookRoom() {
        try {
            if (selectedRoom == null) {
                showAlert(Alert.AlertType.WARNING,
                        text.text("BOOKINGS", "alert_validation_title", "Validation Error"),
                        text.text("BOOKINGS", "alert_validation_message", "Please select a room first."));
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

            String summary = String.format(
                    "%s%n%n%s: %s%n%s: %s%n%s: %s (%s)%n%s: %s%n%s: %s%n%s: %d%n%s: %s",
                    text.text("BOOKINGS", "summary_heading", "Booking Confirmed"),
                    text.text("BOOKINGS", "summary_booking_id_label", "Booking ID"),
                    booking.getBookingId(),
                    text.text("BOOKINGS", "summary_guest_label", "Guest"),
                    booking.getCustomer().getName(),
                    text.text("BOOKINGS", "summary_room_label", "Room"),
                    booking.getRoom().getRoomNumber(),
                    booking.getRoom().getRoomType(),
                    text.text("BOOKINGS", "summary_check_in_label", "Check-In"),
                    booking.getCheckInDate(),
                    text.text("BOOKINGS", "summary_check_out_label", "Check-Out"),
                    booking.getCheckOutDate(),
                    text.text("BOOKINGS", "summary_nights_label", "Nights"),
                    booking.getNights(),
                    text.text("BOOKINGS", "summary_total_charge_label", "Total Charge"),
                    text.money(booking.calculateTotalAmount())
            );
            showAlert(Alert.AlertType.INFORMATION,
                    text.text("BOOKINGS", "alert_booking_successful_title", "Booking Successful"),
                    summary);
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.ERROR,
                    text.text("BOOKINGS", "alert_booking_failed_title", "Booking Failed"),
                    ex.getMessage());
        }
    }

    public void refreshRoomMenu() {
        if (btnRoomSelect == null) {
            return;
        }

        btnRoomSelect.getItems().clear();

        for (RoomType type : RoomType.values()) {
            var roomsOfType = manager.getAvailableRooms().stream()
                    .filter(r -> r.getRoomType() == type)
                    .toList();

            if (!roomsOfType.isEmpty()) {
                Menu categoryMenu = new Menu(type.getDisplayName() + " (" + roomsOfType.size() + ")");
                for (Room room : roomsOfType) {
                    MenuItem item = new MenuItem(
                            text.text("ROOMS", "table_col_room_no", "Room") + " "
                                    + room.getRoomNumber() + " - " + text.money(room.getPricePerDay()));
                    item.setOnAction(e -> {
                        selectedRoom = room;
                        btnRoomSelect.setText(text.text("ROOMS", "table_col_room_no", "Room") + " "
                                + room.getRoomNumber() + " (" + type.getDisplayName() + ")");
                    });
                    categoryMenu.getItems().add(item);
                }
                btnRoomSelect.getItems().add(categoryMenu);
            }
        }

        if (btnRoomSelect.getItems().isEmpty()) {
            MenuItem noRooms = new MenuItem(text.text("BOOKINGS", "menu_no_rooms_available", "No rooms available"));
            noRooms.setDisable(true);
            btnRoomSelect.getItems().add(noRooms);
        }

        if (selectedRoom != null && !manager.getAvailableRooms().contains(selectedRoom)) {
            selectedRoom = null;
            btnRoomSelect.setText(text.text("BOOKINGS", "prompt_select_room", "Choose available room"));
        }
    }

    private void clearForm() {
        tfName.clear();
        tfContact.clear();
        tfEmail.clear();
        selectedRoom = null;
        btnRoomSelect.setText(text.text("BOOKINGS", "prompt_select_room", "Choose available room"));
        dpCheckIn.setValue(LocalDate.now());
        dpCheckOut.setValue(LocalDate.now().plusDays(1));
    }

    private Label makeLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("form-label");
        return lbl;
    }

    private HBox buildLuxuryDateField(DatePicker datePicker) {
        datePicker.getStyleClass().add("lux-date-picker");
        datePicker.setEditable(false);
        HBox.setHgrow(datePicker, Priority.ALWAYS);

        Region icon = new Region();
        icon.getStyleClass().add("lux-date-icon");

        HBox wrapper = new HBox(10, datePicker, icon);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.getStyleClass().add("lux-date-field");
        wrapper.setMinWidth(220);
        wrapper.setPrefWidth(240);
        wrapper.setOnMouseClicked(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            datePicker.requestFocus();
            if (datePicker.isShowing()) {
                datePicker.hide();
            } else {
                datePicker.show();
            }
        });

        datePicker.focusedProperty().addListener((obs, wasFocused, isFocused) ->
                wrapper.pseudoClassStateChanged(FOCUSED, isFocused));
        datePicker.showingProperty().addListener((obs, wasShowing, isShowing) ->
                wrapper.pseudoClassStateChanged(FOCUSED, isShowing || datePicker.isFocused()));

        return wrapper;
    }

    private void configureDatePicker(DatePicker datePicker, boolean checkInPicker) {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().removeAll("lux-today-cell", "lux-disabled-date-cell");

                if (empty || item == null) {
                    setDisable(false);
                    return;
                }

                LocalDate today = LocalDate.now();
                LocalDate minimumDate = checkInPicker
                        ? today
                        : (dpCheckIn != null && dpCheckIn.getValue() != null ? dpCheckIn.getValue().plusDays(1) : today.plusDays(1));

                boolean disabled = item.isBefore(minimumDate);
                setDisable(disabled);
                if (disabled) {
                    getStyleClass().add("lux-disabled-date-cell");
                }

                if (item.equals(today)) {
                    getStyleClass().add("lux-today-cell");
                }
            }
        });

        if (checkInPicker) {
            datePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (dpCheckOut != null) {
                    LocalDate checkout = dpCheckOut.getValue();
                    if (checkout == null || (newValue != null && checkout.isBefore(newValue.plusDays(1)))) {
                        dpCheckOut.setValue(newValue != null ? newValue.plusDays(1) : LocalDate.now().plusDays(1));
                    }
                }
            });
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
