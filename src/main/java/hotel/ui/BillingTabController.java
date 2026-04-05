package hotel.ui;

import hotel.model.Bill;
import hotel.model.Booking;
import hotel.service.BookingManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.text.NumberFormat;
import java.util.Locale;

public class BillingTabController {

    private BookingManager manager;

    @FXML private ComboBox<Booking> bookingComboBox;
    @FXML private TableView<Bill> billsTable;
    @FXML private TableColumn<Bill, String> colBillId;
    @FXML private TableColumn<Bill, String> colBookingRef;
    @FXML private TableColumn<Bill, String> colAmount;
    @FXML private TableColumn<Bill, String> colDate;
    @FXML private TableColumn<Bill, String> colPaid;
    @FXML private TableColumn<Bill, Void> colAction;

    private Tab tab;

    public void init(BookingManager manager) {
        this.manager = manager;
        tab = new Tab("💳  Billing");
        tab.setClosable(false);

        setupTable();
        loadBookings();
    }

    public Tab getTab() { return tab; }

    private void setupTable() {
        billsTable.setItems(manager.getBills());

        colBillId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBillId()));
        colBookingRef.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBooking().getBookingId()));

        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        colAmount.setCellValueFactory(c -> new SimpleStringProperty("₹ " + fmt.format(c.getValue().getTotalAmount())));

        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGenerationDate().toString()));
        colPaid.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isPaid() ? "Paid" : "Pending"));

        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button btnPay = new Button("Mark Paid");
            {
                btnPay.getStyleClass().addAll("btn", "btn-info");
                btnPay.setOnAction(e -> {
                    Bill b = getTableView().getItems().get(getIndex());
                    if (!b.isPaid()) manager.markBillPaid(b);
                    getTableView().refresh();
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) setGraphic(null);
                else {
                    Bill b = getTableView().getItems().get(getIndex());
                    btnPay.setDisable(b.isPaid());
                    setGraphic(btnPay);
                }
            }
        });
    }

    @FXML
    public void loadBookings() {
        if (manager == null) return;
        bookingComboBox.setItems(manager.getBookings());
    }

    @FXML
    public void handleCheckOutAction() {
        Booking selected = bookingComboBox.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Booking Selected", "Please select an active booking.");
            return;
        }

        Bill newBill = manager.checkOutAndBill(selected);
        bookingComboBox.getSelectionModel().clearSelection();
        loadBookings();

        showAlert("Checkout Complete", "Generated Invoice: " + newBill.getBillId() + "\nTotal Amount: ₹" + newBill.getTotalAmount() + "\nRoom is now available.");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
