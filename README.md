# Bus Management & Reservation System

A complete Java Swing desktop application integrated with MySQL JDBC for managing buses, passengers, seat reservations, and ticket generation.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Java (JDK 21+)
- **GUI Framework:** Java Swing (`JFrame`, `JTable`, `JPanel`, `CardLayout`/`BorderLayout`)
- **Database:** MySQL Server 8.0+
- **Database Driver:** `mysql-connector-j-26.7.0.jar`
- **Architecture Pattern:** MVC / DAO Architecture
  - **`main`**: Application entry point (`Main.java`)
  - **`database`**: JDBC connection management (`DBConnection.java`)
  - **`model`**: Domain entities (`Passenger.java`, `Bus.java`, `Booking.java`)
  - **`dao`**: Data Access Objects (`PassengerDAO.java`, `BusDAO.java`, `BookingDAO.java`)
  - **`gui`**: Graphical user interface windows (`LoginFrame.java`, `Dashboard.java`, `PassengerManagement.java`, `BusManagement.java`, `BookingManagement.java`, `TicketGeneration.java`)

---

## 🔑 Default Credentials

- **Username:** `admin`
- **Password:** `1234`

---

## 🗄️ Database Setup

The database script is located at [`lib/database.sql`](file:///c:/Users/ASUS/OneDrive/Desktop/bus%20management%20system/lib/database.sql).

- **Database Name:** `bus_reservation`
- **Tables:**
  - `passenger` (`passenger_id`, `name`, `age`, `gender`, `phone`)
  - `bus` (`bus_id`, `bus_name`, `source`, `destination`, `total_seats`, `fare`)
  - `booking` (`booking_id`, `passenger_id`, `bus_id`, `seat_number`, `booking_date`, `fare`, `status`)

---

## 🚀 How to Run

### Option 1: Run with Docker Compose (Recommended - Zero Setup)
Run the entire application along with the MySQL database in Docker:
```bash
# Start MySQL and the Application
docker compose up --build
```
Once started, open your web browser and navigate to:
👉 **[http://localhost:6969](http://localhost:6969)**

The interactive Java Swing GUI will load automatically right in your browser via noVNC!
- **Default Username:** `admin`
- **Default Password:** `1234`

To stop:
```bash
docker compose down
```

---

### Option 2: Run directly with Java (Local)
```powershell
# From the project root directory:
java -cp "bin;lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar" main.Main
```

### Option 3: Recompile and Run Locally
```powershell
# Compile all source files
$files = (Get-ChildItem -Recurse src -Filter *.java).FullName
javac -cp "lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar" -d bin $files

# Run
java -cp "bin;lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar" main.Main
```

---

## ✨ Features

1. **Secure Admin Authentication:**
   - Dedicated login window with prefilled default credentials and Enter key submit support.
2. **Interactive Admin Dashboard:**
   - Quick navigation cards for all 4 subsystems.
   - Real-time system statistics counter (total registered buses, passengers, bookings).
3. **Passenger Management:**
   - Register new passengers, view list in a responsive `JTable`.
   - Update passenger information or remove passenger records.
   - Select row to pre-fill input fields.
4. **Bus Management:**
   - Manage buses with name, source, destination, total seating capacity, and fare.
   - Real-time `JTable` display with row click selection.
   - Full CRUD operations connected directly to MySQL.
5. **Seat Booking & Reservation:**
   - Select Bus from dropdown to automatically view route, fare, and available vs total seats.
   - Select Passenger from dropdown.
   - Seat number validation preventing double bookings of occupied seats.
   - Cancel existing bookings.
   - Direct button to generate ticket for any selected reservation.
6. **Ticket Generation & Boarding Pass:**
   - View formatted boarding pass / ticket preview with passenger, trip, route, and payment details.
   - Search by Booking ID or pick from the bookings list.
   - Integrated Java Print API to print directly to physical printer / PDF.
   - Save ticket receipt to a `.txt` file.
