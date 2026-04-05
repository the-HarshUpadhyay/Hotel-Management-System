package hotel.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a hotel room booking.
 * Calculates the total bill from room price and stay duration.
 *
 * Demonstrates: composition (has-a Customer, has-a Room),
 * and encapsulation via private fields + getters/setters.
 */
public class Booking {

    private String     bookingId;
    private Customer   customer;
    private Room       room;
    private LocalDate  checkInDate;
    private LocalDate  checkOutDate;

    // ---- Constructors ----

    public Booking() {}

    public Booking(String bookingId, Customer customer, Room room,
                   LocalDate checkInDate, LocalDate checkOutDate) {
        this.bookingId    = bookingId;
        this.customer     = customer;
        this.room         = room;
        this.checkInDate  = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    // ---- Business Logic ----

    /**
     * Calculates total amount based on room price per day and number of nights.
     * @return total charge; 0 if dates are invalid
     */
    public double calculateTotalAmount() {
        if (checkInDate == null || checkOutDate == null ||
                !checkOutDate.isAfter(checkInDate)) {
            return 0.0;
        }
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        return nights * room.getPricePerDay();
    }

    /**
     * Convenience method returning number of nights stayed.
     */
    public long getNights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    // ---- Getters / Setters ----

    public String    getBookingId()              { return bookingId; }
    public void      setBookingId(String id)     { this.bookingId = id; }

    public Customer  getCustomer()               { return customer; }
    public void      setCustomer(Customer c)     { this.customer = c; }

    public Room      getRoom()                   { return room; }
    public void      setRoom(Room r)             { this.room = r; }

    public LocalDate getCheckInDate()            { return checkInDate; }
    public void      setCheckInDate(LocalDate d) { this.checkInDate = d; }

    public LocalDate getCheckOutDate()           { return checkOutDate; }
    public void      setCheckOutDate(LocalDate d){ this.checkOutDate = d; }

    @Override
    public String toString() {
        return String.format("Booking[%s] %s → Room %s | ₹%.2f",
                bookingId, customer.getName(),
                room.getRoomNumber(), calculateTotalAmount());
    }
}
