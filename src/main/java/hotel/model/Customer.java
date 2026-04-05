package hotel.model;

/**
 * Represents a hotel guest / customer.
 * Follows encapsulation: fields are private with public getters/setters.
 */
public class Customer {

    private String name;
    private String contactNumber;
    private String email;

    // ---- Constructors ----

    public Customer() {}

    public Customer(String name, String contactNumber, String email) {
        this.name          = name;
        this.contactNumber = contactNumber;
        this.email         = email;
    }

    // ---- Getters / Setters ----

    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }

    public String getContactNumber()         { return contactNumber; }
    public void setContactNumber(String c)   { this.contactNumber = c; }

    public String getEmail()                 { return email; }
    public void setEmail(String email)       { this.email = email; }

    @Override
    public String toString() {
        return name + " (" + contactNumber + ")";
    }
}
