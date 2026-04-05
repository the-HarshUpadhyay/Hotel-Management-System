# 🏨 Hotel Management System

A fully-featured **JavaFX desktop application** for managing hotel rooms, bookings, and customer records. Built as a university lab mini-project demonstrating OOP principles, JavaFX GUI development, and file-based data persistence.

---

## 📸 Features

### 🏨 Room Management
- Add rooms with Number, Type (Single / Double / Deluxe), and Price per day
- View all rooms in a sortable table with colour-coded availability status
- Filter to show **available rooms only**
- Delete rooms (occupied rooms are protected)

### 📋 Booking & Checkout
- Book a room for a guest with check-in / check-out date pickers
- ComboBox auto-updates to show only available rooms
- Booking confirmation popup with full summary and total charges
- One-click **Checkout** to free a room

### 👤 Customer Records
- Full booking history with 10-column table
- **Live search** by guest name or contact number
- Revenue summary bar (auto-updates on any booking change)

### 💾 Persistence
- Data auto-saved to `hotel_data.json` after every action
- State fully restored on next launch
- 10 sample rooms pre-loaded on first run

---

## 🗂️ Project Structure

```
HotelManagementSystem/
├── pom.xml
└── src/main/
    ├── java/hotel/
    │   ├── HotelApp.java               ← Main entry point
    │   ├── model/
    │   │   ├── RoomType.java           ← Enum: SINGLE, DOUBLE, DELUXE
    │   │   ├── Room.java               ← Observable model (JavaFX Properties)
    │   │   ├── Customer.java           ← Guest data
    │   │   └── Booking.java            ← Booking with auto bill calculation
    │   ├── service/
    │   │   └── BookingManager.java     ← Business logic + JSON persistence
    │   └── ui/
    │       ├── RoomTab.java            ← Room Management tab
    │       ├── BookingTab.java         ← Booking & Checkout tab
    │       └── CustomerTab.java        ← Customer Records tab
    └── resources/hotel/
        └── styles.css                  ← UI stylesheet
```

---

## ⚙️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| GUI Framework | JavaFX 21 |
| Build Tool | Maven 3.8+ |
| JSON Persistence | Gson 2.10 |

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+** — verify with `java --version`
- **Maven 3.8+** — verify with `mvn --version`  
  *(or use the included Maven Wrapper — no install required)*

### Run the application

```bash
# Clone / navigate to the project
cd "HotelManagementSystem"

# With Maven installed
mvn clean javafx:run

# With the Maven Wrapper (no Maven install needed)
./mvnw clean javafx:run
```

The app opens to a 3-tab interface. Data is saved to `hotel_data.json` in the working directory.

---

## 🧠 OOP Concepts Demonstrated

| Concept | Implementation |
|---------|---------------|
| **Encapsulation** | All model fields are `private` with getters/setters |
| **Inheritance** | `HotelApp extends Application` |
| **Composition** | `Booking` has-a `Customer` and has-a `Room` |
| **Abstraction** | UI tabs delegate all logic to `BookingManager` |
| **Enum** | `RoomType` replaces magic strings |
| **Observer Pattern** | `ObservableList` + `ListChangeListener` for live UI updates |

---

## ✅ Input Validation

- Room number must be unique and non-empty
- Price must be a positive number
- Customer name cannot be blank
- Contact number must be exactly 10 digits
- Check-out date must be after check-in date
- Cannot book an already occupied room
- Cannot delete an occupied room

---

## 📁 Data File

On first launch, `hotel_data.json` is created in the working directory. It stores all rooms and active bookings. Safe to delete to reset to sample data.

---

## 👨‍💻 Author

**Lab Mini Project** — Operating Systems & Distributed Labs (OSDL)  
University Course Project | April 2026
