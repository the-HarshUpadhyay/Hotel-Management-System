# Hotel Management System

A JavaFX desktop dashboard for managing rooms, reservations, guests, billing, and printed receipts with JDBC-backed persistence.

## Overview

This project is a premium-styled hotel operations app inspired by luxury hospitality workflows. It includes room inventory management, reservation handling, guest records, checkout billing, receipt printing, and a dark themed UI designed for an internal front-desk experience.

The application is now fully JDBC-based for operational data and UI copy. It can run with SQLite by default or connect to PostgreSQL/MySQL through environment configuration.

## Features

- Room inventory with pricing, availability status, filtering, and protected deletion for occupied rooms
- Reservation flow with guest details, luxury-styled date pickers, disabled past dates, and highlighted current day
- Guest registry with live search and revenue summary
- Billing workflow with checkout invoice generation, payment marking, and receipt printing
- JDBC-backed persistence for rooms, customers, bookings, bills, and UI text content
- Database-seeded UI copy through the `app_texts` table

## Tech Stack

- Java 22
- JavaFX 21
- Maven
- JDBC
- SQLite by default
- PostgreSQL / MySQL supported through JDBC configuration

## Project Structure

```text
HotelManagementSystem/
|-- pom.xml
`-- src/main/
    |-- java/hotel/
    |   |-- HotelApp.java
    |   |-- config/
    |   |   `-- AppText.java
    |   |-- model/
    |   |   |-- Bill.java
    |   |   |-- Booking.java
    |   |   |-- Customer.java
    |   |   |-- Room.java
    |   |   `-- RoomType.java
    |   |-- service/
    |   |   |-- BookingManager.java
    |   |   `-- DatabaseHelper.java
    |   `-- ui/
    |       |-- BillingTabController.java
    |       |-- BookingTab.java
    |       |-- CustomerTab.java
    |       `-- RoomTab.java
    `-- resources/hotel/
        |-- styles.css
        `-- ui/BillingTab.fxml
```

## Requirements

- Java 22 or newer
- Maven 3.8+

## Running

Default run:

```bash
mvn clean javafx:run
```

If you prefer the Maven wrapper:

```bash
./mvnw clean javafx:run
```

On Windows:

```powershell
.\mvnw.cmd clean javafx:run
```

## Database Configuration

If no database environment variables are set, the app falls back to:

```text
jdbc:sqlite:database.sqlite
```

To use a server database instead, set these environment variables before launch:

```bash
HOTEL_DB_URL=jdbc:postgresql://localhost:5432/hotel_management
HOTEL_DB_USER=your_user
HOTEL_DB_PASSWORD=your_password
```

Example MySQL URL:

```bash
HOTEL_DB_URL=jdbc:mysql://localhost:3306/hotel_management
```

## JDBC Tables

The application initializes and uses these tables:

- `rooms`
- `customers`
- `bookings`
- `bills`
- `app_texts`

## Notes

- Sample rooms are seeded automatically when the database is empty.
- UI copy is loaded from the database instead of local text files.
- Receipt printing is triggered from the billing workflow after settlement.
- SQLite is still JDBC-based but file-backed; use PostgreSQL or MySQL if you want a non-file database deployment.
