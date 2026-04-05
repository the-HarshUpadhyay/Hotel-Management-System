package hotel.model;

import java.time.LocalDate;

/**
 * Represents an invoice generated upon check-out for a Booking.
 */
public class Bill {
    private String billId;
    private Booking booking;
    private double totalAmount;
    private LocalDate generationDate;
    private boolean isPaid;

    public Bill(String billId, Booking booking, double totalAmount, LocalDate generationDate, boolean isPaid) {
        this.billId = billId;
        this.booking = booking;
        this.totalAmount = totalAmount;
        this.generationDate = generationDate;
        this.isPaid = isPaid;
    }

    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public LocalDate getGenerationDate() { return generationDate; }
    public void setGenerationDate(LocalDate date) { this.generationDate = date; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    @Override
    public String toString() {
        return "Bill{" +
                "billId='" + billId + '\'' +
                ", amount=" + totalAmount +
                ", isPaid=" + isPaid +
                '}';
    }
}
