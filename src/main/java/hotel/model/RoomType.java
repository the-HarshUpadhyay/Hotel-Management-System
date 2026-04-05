package hotel.model;

/**
 * Enumeration of available room categories in the hotel.
 */
public enum RoomType {
    SINGLE("Single"),
    DOUBLE("Double"),
    DELUXE("Deluxe");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
