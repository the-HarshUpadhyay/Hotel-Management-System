package hotel.model;

import javafx.beans.property.*;

/**
 * Represents a hotel room with JavaFX observable properties
 * for seamless TableView data binding.
 *
 * Demonstrates encapsulation: all fields are private, accessed via
 * JavaFX property accessors and standard getters/setters.
 */
public class Room {

    private final StringProperty roomNumber;
    private final ObjectProperty<RoomType> roomType;
    private final DoubleProperty pricePerDay;
    private final BooleanProperty available;

    // ---- Constructors ----

    public Room() {
        this("", RoomType.SINGLE, 0.0, true);
    }

    public Room(String roomNumber, RoomType roomType, double pricePerDay, boolean available) {
        this.roomNumber   = new SimpleStringProperty(roomNumber);
        this.roomType     = new SimpleObjectProperty<>(roomType);
        this.pricePerDay  = new SimpleDoubleProperty(pricePerDay);
        this.available    = new SimpleBooleanProperty(available);
    }

    // ---- JavaFX Property Accessors (required for TableView cell factories) ----

    public StringProperty roomNumberProperty()        { return roomNumber; }
    public ObjectProperty<RoomType> roomTypeProperty(){ return roomType; }
    public DoubleProperty pricePerDayProperty()       { return pricePerDay; }
    public BooleanProperty availableProperty()        { return available; }

    // ---- Standard Getters / Setters ----

    public String getRoomNumber()           { return roomNumber.get(); }
    public void setRoomNumber(String v)     { roomNumber.set(v); }

    public RoomType getRoomType()           { return roomType.get(); }
    public void setRoomType(RoomType v)     { roomType.set(v); }

    public double getPricePerDay()          { return pricePerDay.get(); }
    public void setPricePerDay(double v)    { pricePerDay.set(v); }

    public boolean isAvailable()            { return available.get(); }
    public void setAvailable(boolean v)     { available.set(v); }

    /** Convenience display for ComboBox listing */
    @Override
    public String toString() {
        return String.format("Room %s (%s) - ₹%.0f/day",
                getRoomNumber(), getRoomType().getDisplayName(), getPricePerDay());
    }
}
