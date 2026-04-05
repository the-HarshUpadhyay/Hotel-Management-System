package hotel.ui;

import hotel.config.AppText;
import hotel.model.Bill;
import hotel.model.Booking;
import hotel.service.BookingManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;

public class BillingTabController {
    private static final DateTimeFormatter RECEIPT_DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private BookingManager manager;
    private final AppText text = AppText.get();

    @FXML private ComboBox<Booking> bookingComboBox;
    @FXML private Label billingTitleLabel;
    @FXML private Label selectActiveBookingLabel;
    @FXML private Button checkOutButton;
    @FXML private Button refreshButton;
    @FXML private Label recentInvoicesLabel;
    @FXML private TableView<Bill> billsTable;
    @FXML private Label billsPlaceholderLabel;
    @FXML private TableColumn<Bill, String> colBillId;
    @FXML private TableColumn<Bill, String> colBookingRef;
    @FXML private TableColumn<Bill, String> colAmount;
    @FXML private TableColumn<Bill, String> colDate;
    @FXML private TableColumn<Bill, String> colPaid;
    @FXML private TableColumn<Bill, Void> colAction;

    public void init(BookingManager manager) {
        this.manager = manager;
        applyText();
        setupBookingCombo();
        setupTable();
        loadBookings();
    }

    public void refreshData() {
        loadBookings();
        billsTable.refresh();
    }

    private void setupBookingCombo() {
        bookingComboBox.setPromptText(text.text("BILLING", "prompt_choose_booking", "Choose a booking to check out"));
        bookingComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Booking booking) {
                if (booking == null) {
                    return "";
                }
                return booking.getBookingId() + " - Room " + booking.getRoom().getRoomNumber()
                        + " - " + booking.getCustomer().getName();
            }

            @Override
            public Booking fromString(String string) {
                return null;
            }
        });
    }

    private void setupTable() {
        billsTable.setItems(manager.getBills());
        colBillId.setText(text.text("BILLING", "table_col_invoice_id", "Invoice ID"));
        colBookingRef.setText(text.text("BILLING", "table_col_booking_ref", "Booking Ref"));
        colAmount.setText(text.text("BILLING", "table_col_amount", "Amount"));
        colDate.setText(text.text("BILLING", "table_col_date_generated", "Date Generated"));
        colPaid.setText(text.text("BILLING", "table_col_status", "Status"));
        colAction.setText(text.text("BILLING", "table_col_action", "Action"));

        colBillId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBillId()));
        colBookingRef.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBooking().getBookingId()));

        colAmount.setCellValueFactory(c -> new SimpleStringProperty(text.money(c.getValue().getTotalAmount())));

        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGenerationDate().toString()));
        colPaid.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isPaid()
                ? text.text("BILLING", "status_paid", "Paid")
                : text.text("BILLING", "status_pending", "Pending")));

        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button btnPay = new Button();

            {
                btnPay.getStyleClass().addAll("btn", "btn-info");
                btnPay.setOnAction(e -> {
                    Bill bill = getTableView().getItems().get(getIndex());
                    handleBillAction(bill);
                    getTableView().refresh();
                });
            }

            @Override
            protected void updateItem(Void value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Bill bill = getTableView().getItems().get(getIndex());
                    btnPay.setText(bill.isPaid()
                            ? text.text("BILLING", "button_print_receipt", "Print Receipt")
                            : text.text("BILLING", "button_mark_paid", "Mark Paid"));
                    setGraphic(btnPay);
                }
            }
        });
    }

    @FXML
    public void loadBookings() {
        if (manager == null) {
            return;
        }
        bookingComboBox.setItems(manager.getBookings());
    }

    @FXML
    public void handleCheckOutAction() {
        Booking selected = bookingComboBox.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(text.text("BILLING", "alert_no_booking_selected_title", "No Booking Selected"),
                    text.text("BILLING", "alert_no_booking_selected_message", "Please select an active booking."));
            return;
        }

        Bill newBill = manager.checkOutAndBill(selected);
        bookingComboBox.getSelectionModel().clearSelection();
        loadBookings();
        billsTable.refresh();

        showAlert(text.text("BILLING", "alert_checkout_complete_title", "Checkout Complete"),
                text.format("BILLING", "alert_checkout_complete_line_1", "Generated invoice: {billId}",
                        AppText.tokens("billId", newBill.getBillId()))
                        + "\n"
                        + text.format("BILLING", "alert_checkout_complete_line_2", "Total amount: \u20b9 {amount}",
                        AppText.tokens("amount", text.money(newBill.getTotalAmount()).replace("\u20b9 ", "")))
                        + "\n"
                        + text.text("BILLING", "alert_checkout_complete_line_3", "Room is now available."));
    }

    private void handleBillAction(Bill bill) {
        if (bill == null) {
            return;
        }

        boolean wasUnpaid = !bill.isPaid();
        if (wasUnpaid) {
            manager.markBillPaid(bill);
        }

        boolean printed = printReceipt(bill);
        if (!printed && wasUnpaid) {
            showAlert(text.text("BILLING", "alert_receipt_pending_title", "Payment Recorded"),
                    text.text("BILLING", "alert_receipt_pending_message",
                            "Payment was recorded, but the receipt was not printed."));
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean printReceipt(Bill bill) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            showAlert(text.text("BILLING", "alert_print_unavailable_title", "Printer Unavailable"),
                    text.text("BILLING", "alert_print_unavailable_message",
                            "No printer is available for receipt printing."));
            return false;
        }

        Window window = billsTable.getScene() == null ? null : billsTable.getScene().getWindow();
        if (!job.showPrintDialog(window)) {
            return false;
        }

        VBox receiptNode = buildReceiptNode(bill);
        boolean success = job.printPage(receiptNode);
        if (success) {
            job.endJob();
        } else {
            job.cancelJob();
            showAlert(text.text("BILLING", "alert_receipt_failed_title", "Print Failed"),
                    text.text("BILLING", "alert_receipt_failed_message",
                            "The receipt could not be sent to the printer."));
        }
        return success;
    }

    private VBox buildReceiptNode(Bill bill) {
        Booking booking = bill.getBooking();

        Label hotelName = new Label(text.text("HEADER", "brand_wordmark_expanded", "Hotel Command Center"));
        hotelName.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #17323a;");

        Label hotelSub = new Label(text.text("HEADER", "brand_support",
                "Modern front desk workspace for rooms, guests, bookings, and billing"));
        hotelSub.setWrapText(true);
        hotelSub.setTextAlignment(TextAlignment.CENTER);
        hotelSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #52656b;");

        Label receiptTitle = new Label(text.text("BILLING", "receipt_title", "Payment Receipt"));
        receiptTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #17323a;");

        VBox details = new VBox(8,
                receiptLine(text.text("BILLING", "receipt_invoice_id", "Invoice ID"), bill.getBillId()),
                receiptLine(text.text("BILLING", "receipt_booking_id", "Booking ID"), booking.getBookingId()),
                receiptLine(text.text("BILLING", "receipt_guest_name", "Guest"), booking.getCustomer().getName()),
                receiptLine(text.text("BILLING", "receipt_contact", "Contact"), booking.getCustomer().getContactNumber()),
                receiptLine(text.text("BILLING", "receipt_email", "Email"), booking.getCustomer().getEmail()),
                receiptLine(text.text("BILLING", "receipt_room", "Room"),
                        booking.getRoom().getRoomNumber() + " (" + booking.getRoom().getRoomType().getDisplayName() + ")"),
                receiptLine(text.text("BILLING", "receipt_check_in", "Check-In"),
                        booking.getCheckInDate().format(RECEIPT_DATE_FMT)),
                receiptLine(text.text("BILLING", "receipt_check_out", "Check-Out"),
                        booking.getCheckOutDate().format(RECEIPT_DATE_FMT)),
                receiptLine(text.text("BILLING", "receipt_nights", "Nights"), String.valueOf(booking.getNights())),
                receiptLine(text.text("BILLING", "receipt_payment_date", "Payment Date"),
                        bill.getGenerationDate().format(RECEIPT_DATE_FMT)),
                receiptLine(text.text("BILLING", "receipt_status", "Status"),
                        text.text("BILLING", "status_paid", "Paid"))
        );

        Label totalLabel = new Label(
                text.text("BILLING", "receipt_total", "Total Paid") + ": " + text.money(bill.getTotalAmount()));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #17323a;");

        Label footer = new Label(text.text("BILLING", "receipt_footer",
                "Thank you for choosing Taj Mahal hospitality."));
        footer.setWrapText(true);
        footer.setTextAlignment(TextAlignment.CENTER);
        footer.setStyle("-fx-font-size: 11px; -fx-text-fill: #52656b;");

        Separator separator = new Separator();

        VBox receipt = new VBox(14, hotelName, hotelSub, receiptTitle, separator, details, totalLabel, footer);
        receipt.setAlignment(Pos.TOP_CENTER);
        receipt.setPadding(new Insets(28));
        receipt.setPrefWidth(420);
        receipt.setMinWidth(420);
        receipt.setMaxWidth(420);
        receipt.setStyle("-fx-background-color: white;");
        return receipt;
    }

    private VBox receiptLine(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #52656b;");

        Label value = new Label(valueText);
        value.setWrapText(true);
        value.setMaxWidth(Double.MAX_VALUE);
        value.setStyle("-fx-font-size: 13px; -fx-text-fill: #17323a;");

        VBox line = new VBox(2, label, value);
        line.setFillWidth(true);
        return line;
    }

    private void applyText() {
        billingTitleLabel.setText(text.text("BILLING", "section_billing_and_checkout_management",
                "Billing and Checkout Management"));
        selectActiveBookingLabel.setText(text.text("BILLING", "label_select_active_booking", "Select Active Booking:"));
        checkOutButton.setText(text.text("BILLING", "button_check_out_and_bill", "Check Out and Bill"));
        refreshButton.setText(text.text("BILLING", "button_refresh", "Refresh"));
        recentInvoicesLabel.setText(text.text("BILLING", "section_recent_invoices", "Recent Invoices"));
        billsPlaceholderLabel.setText(text.text("BILLING", "table_placeholder", "No invoices generated."));
    }
}
