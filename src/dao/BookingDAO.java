package dao;

import database.DBConnection;
import model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class BookingDAO {

    private static final AtomicInteger nextGeneratedBookingId = new AtomicInteger(1005);
    private static final List<Booking> inMemoryBookings = new CopyOnWriteArrayList<>();

    static {
        Booking b1 = new Booking(1001, 101, 2, 1, 12, "2026-10-15", 45.00, "CONFIRMED", 0.0, false);
        b1.setPassengerName("John Doe"); b1.setPassengerGender("Male"); b1.setPassengerPhone("9876543210");
        b1.setBusName("Express Volvo AC"); b1.setSource("New York"); b1.setDestination("Boston");
        inMemoryBookings.add(b1);

        Booking b2 = new Booking(1002, 102, 2, 1, 11, "2026-10-15", 55.00, "CONFIRMED", 0.0, false);
        b2.setPassengerName("Sarah Smith"); b2.setPassengerGender("Female"); b2.setPassengerPhone("9123456780");
        b2.setBusName("Express Volvo AC"); b2.setSource("New York"); b2.setDestination("Boston");
        inMemoryBookings.add(b2);

        Booking b3 = new Booking(1003, 104, 2, 1, 7, "2026-10-15", 22.50, "CONFIRMED", 22.50, true);
        b3.setPassengerName("Robert Williams"); b3.setPassengerGender("Male"); b3.setPassengerPhone("9811223344");
        b3.setPassengerDisability(true); b3.setDisabilityType("Locomotor / Mobility"); b3.setDisabilityId("UDID-98765-2026");
        b3.setBusName("Express Volvo AC"); b3.setSource("New York"); b3.setDestination("Boston");
        inMemoryBookings.add(b3);
    }

    public boolean addBooking(Booking b) {
        String query = "INSERT INTO booking (passenger_id, user_id, bus_id, seat_number, booking_date, fare, status, concession_amount, is_concession) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                b.setBookingId(nextGeneratedBookingId.incrementAndGet());
                inMemoryBookings.add(0, b);
                return true;
            }
            try (PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pst.setInt(1, b.getPassengerId());
                if (b.getUserId() != null) {
                    pst.setInt(2, b.getUserId());
                } else {
                    pst.setNull(2, Types.INTEGER);
                }
                pst.setInt(3, b.getBusId());
                pst.setInt(4, b.getSeatNumber());
                pst.setString(5, b.getBookingDate());
                pst.setDouble(6, b.getFare());
                pst.setString(7, b.getStatus() != null ? b.getStatus() : "CONFIRMED");
                pst.setDouble(8, b.getConcessionAmount());
                pst.setBoolean(9, b.isConcession());

                int rows = pst.executeUpdate();
                if (rows > 0) {
                    try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            b.setBookingId(generatedKeys.getInt(1));
                        }
                    }
                    return true;
                }
            } catch (SQLException ex) {
                // Fallback to legacy insert if concession columns don't exist yet
                String legacy = "INSERT INTO booking (passenger_id, user_id, bus_id, seat_number, booking_date, fare, status) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pst2 = con.prepareStatement(legacy, Statement.RETURN_GENERATED_KEYS)) {
                    pst2.setInt(1, b.getPassengerId());
                    if (b.getUserId() != null) {
                        pst2.setInt(2, b.getUserId());
                    } else {
                        pst2.setNull(2, Types.INTEGER);
                    }
                    pst2.setInt(3, b.getBusId());
                    pst2.setInt(4, b.getSeatNumber());
                    pst2.setString(5, b.getBookingDate());
                    pst2.setDouble(6, b.getFare());
                    pst2.setString(7, b.getStatus() != null ? b.getStatus() : "CONFIRMED");

                    int rows = pst2.executeUpdate();
                    if (rows > 0) {
                        try (ResultSet generatedKeys = pst2.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                b.setBookingId(generatedKeys.getInt(1));
                            }
                        }
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] addBooking error: " + e.getMessage());
        }
        return false;
    }

    public List<Booking> addBookingsBatch(List<Booking> bookings) {
        List<Booking> saved = new ArrayList<>();
        if (bookings == null || bookings.isEmpty()) return saved;

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                for (Booking b : bookings) {
                    b.setBookingId(nextGeneratedBookingId.incrementAndGet());
                    inMemoryBookings.add(0, b);
                    saved.add(b);
                }
                return saved;
            }
            boolean originalAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                String query = "INSERT INTO booking (passenger_id, user_id, bus_id, seat_number, booking_date, fare, status, concession_amount, is_concession) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                for (Booking b : bookings) {
                    try (PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                        pst.setInt(1, b.getPassengerId());
                        if (b.getUserId() != null) {
                            pst.setInt(2, b.getUserId());
                        } else {
                            pst.setNull(2, Types.INTEGER);
                        }
                        pst.setInt(3, b.getBusId());
                        pst.setInt(4, b.getSeatNumber());
                        pst.setString(5, b.getBookingDate());
                        pst.setDouble(6, b.getFare());
                        pst.setString(7, b.getStatus() != null ? b.getStatus() : "CONFIRMED");
                        pst.setDouble(8, b.getConcessionAmount());
                        pst.setBoolean(9, b.isConcession());

                        int rows = pst.executeUpdate();
                        if (rows > 0) {
                            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    b.setBookingId(generatedKeys.getInt(1));
                                }
                            }
                            saved.add(b);
                        }
                    }
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                System.err.println("[BookingDAO] Batch booking failed, rolled back: " + ex.getMessage());
                // Fallback to sequential individual adds
                saved.clear();
                for (Booking b : bookings) {
                    if (addBooking(b)) {
                        saved.add(b);
                    }
                }
            } finally {
                con.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] addBookingsBatch error: " + e.getMessage());
        }
        return saved;
    }

    private Booking mapBookingRow(ResultSet rs) throws SQLException {
        int uId = rs.getInt("user_id");
        Integer userId = rs.wasNull() ? null : uId;

        double concessionAmt = 0.0;
        boolean isConcession = false;
        try {
            concessionAmt = rs.getDouble("concession_amount");
            isConcession = rs.getBoolean("is_concession");
        } catch (SQLException ignored) {}

        Booking b = new Booking(
                rs.getInt("booking_id"),
                rs.getInt("passenger_id"),
                userId,
                rs.getInt("bus_id"),
                rs.getInt("seat_number"),
                rs.getString("booking_date"),
                rs.getDouble("fare"),
                rs.getString("status"),
                concessionAmt,
                isConcession
        );

        b.setPassengerName(rs.getString("passenger_name"));
        b.setPassengerPhone(rs.getString("passenger_phone"));

        try {
            b.setPassengerGender(rs.getString("passenger_gender"));
            b.setPassengerAge(rs.getInt("passenger_age"));
            b.setPassengerDisability(rs.getBoolean("passenger_disability"));
            b.setDisabilityType(rs.getString("disability_type"));
            b.setDisabilityId(rs.getString("disability_id"));
        } catch (SQLException ignored) {}

        b.setBusName(rs.getString("bus_name"));
        b.setSource(rs.getString("source"));
        b.setDestination(rs.getString("destination"));
        return b;
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String query = "SELECT b.booking_id, b.passenger_id, b.user_id, b.bus_id, b.seat_number, b.booking_date, b.fare, b.status, " +
                       "b.concession_amount, b.is_concession, " +
                       "p.name AS passenger_name, p.phone AS passenger_phone, p.gender AS passenger_gender, p.age AS passenger_age, " +
                       "p.is_disability AS passenger_disability, p.disability_type, p.disability_id, " +
                       "bs.bus_name, bs.source, bs.destination " +
                       "FROM booking b " +
                       "LEFT JOIN passenger p ON b.passenger_id = p.passenger_id " +
                       "LEFT JOIN bus bs ON b.bus_id = bs.bus_id " +
                       "ORDER BY b.booking_id DESC";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return new ArrayList<>(inMemoryBookings);
            try (PreparedStatement pst = con.prepareStatement(query);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBookingRow(rs));
                }
            } catch (SQLException ex) {
                // Fallback for legacy database schema before column migrations
                return getAllBookingsLegacy(con);
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] getAllBookings error: " + e.getMessage());
        }
        return list.isEmpty() ? new ArrayList<>(inMemoryBookings) : list;
    }

    private List<Booking> getAllBookingsLegacy(Connection con) {
        List<Booking> list = new ArrayList<>();
        String legacy = "SELECT b.booking_id, b.passenger_id, b.user_id, b.bus_id, b.seat_number, b.booking_date, b.fare, b.status, " +
                        "p.name AS passenger_name, p.phone AS passenger_phone, " +
                        "bs.bus_name, bs.source, bs.destination " +
                        "FROM booking b " +
                        "LEFT JOIN passenger p ON b.passenger_id = p.passenger_id " +
                        "LEFT JOIN bus bs ON b.bus_id = bs.bus_id " +
                        "ORDER BY b.booking_id DESC";
        try (PreparedStatement pst = con.prepareStatement(legacy);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(mapBookingRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] getAllBookingsLegacy error: " + e.getMessage());
        }
        return list;
    }

    public List<Booking> getBookingsByUserId(int userId) {
        List<Booking> list = new ArrayList<>();
        String query = "SELECT b.booking_id, b.passenger_id, b.user_id, b.bus_id, b.seat_number, b.booking_date, b.fare, b.status, " +
                       "b.concession_amount, b.is_concession, " +
                       "p.name AS passenger_name, p.phone AS passenger_phone, p.gender AS passenger_gender, p.age AS passenger_age, " +
                       "p.is_disability AS passenger_disability, p.disability_type, p.disability_id, " +
                       "bs.bus_name, bs.source, bs.destination " +
                       "FROM booking b " +
                       "LEFT JOIN passenger p ON b.passenger_id = p.passenger_id " +
                       "LEFT JOIN bus bs ON b.bus_id = bs.bus_id " +
                       "WHERE b.user_id = ? " +
                       "ORDER BY b.booking_id DESC";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                for (Booking b : inMemoryBookings) {
                    if (b.getUserId() != null && b.getUserId() == userId) {
                        list.add(b);
                    }
                }
                return list;
            }
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapBookingRow(rs));
                    }
                }
            } catch (SQLException ex) {
                // Fallback to legacy query
                return getBookingsByUserIdLegacy(con, userId);
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] getBookingsByUserId error: " + e.getMessage());
        }
        if (list.isEmpty()) {
            for (Booking b : inMemoryBookings) {
                if (b.getUserId() != null && b.getUserId() == userId) {
                    list.add(b);
                }
            }
        }
        return list;
    }

    private List<Booking> getBookingsByUserIdLegacy(Connection con, int userId) {
        List<Booking> list = new ArrayList<>();
        String legacy = "SELECT b.booking_id, b.passenger_id, b.user_id, b.bus_id, b.seat_number, b.booking_date, b.fare, b.status, " +
                        "p.name AS passenger_name, p.phone AS passenger_phone, " +
                        "bs.bus_name, bs.source, bs.destination " +
                        "FROM booking b " +
                        "LEFT JOIN passenger p ON b.passenger_id = p.passenger_id " +
                        "LEFT JOIN bus bs ON b.bus_id = bs.bus_id " +
                        "WHERE b.user_id = ? " +
                        "ORDER BY b.booking_id DESC";
        try (PreparedStatement pst = con.prepareStatement(legacy)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBookingRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] getBookingsByUserIdLegacy error: " + e.getMessage());
        }
        return list;
    }

    public Booking getBookingById(int id) {
        String query = "SELECT b.booking_id, b.passenger_id, b.user_id, b.bus_id, b.seat_number, b.booking_date, b.fare, b.status, " +
                       "b.concession_amount, b.is_concession, " +
                       "p.name AS passenger_name, p.phone AS passenger_phone, p.gender AS passenger_gender, p.age AS passenger_age, " +
                       "p.is_disability AS passenger_disability, p.disability_type, p.disability_id, " +
                       "bs.bus_name, bs.source, bs.destination " +
                       "FROM booking b " +
                       "LEFT JOIN passenger p ON b.passenger_id = p.passenger_id " +
                       "LEFT JOIN bus bs ON b.bus_id = bs.bus_id " +
                       "WHERE b.booking_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                for (Booking b : inMemoryBookings) {
                    if (b.getBookingId() == id) return b;
                }
                return null;
            }
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, id);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return mapBookingRow(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] getBookingById error: " + e.getMessage());
        }
        for (Booking b : inMemoryBookings) {
            if (b.getBookingId() == id) return b;
        }
        return null;
    }

    public Booking getBookingByIdAndUserId(int id, int userId) {
        String query = "SELECT b.booking_id, b.passenger_id, b.user_id, b.bus_id, b.seat_number, b.booking_date, b.fare, b.status, " +
                       "b.concession_amount, b.is_concession, " +
                       "p.name AS passenger_name, p.phone AS passenger_phone, p.gender AS passenger_gender, p.age AS passenger_age, " +
                       "p.is_disability AS passenger_disability, p.disability_type, p.disability_id, " +
                       "bs.bus_name, bs.source, bs.destination " +
                       "FROM booking b " +
                       "LEFT JOIN passenger p ON b.passenger_id = p.passenger_id " +
                       "LEFT JOIN bus bs ON b.bus_id = bs.bus_id " +
                       "WHERE b.booking_id = ? AND b.user_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                for (Booking b : inMemoryBookings) {
                    if (b.getBookingId() == id && b.getUserId() != null && b.getUserId() == userId) {
                        return b;
                    }
                }
                return null;
            }
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, id);
                pst.setInt(2, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return mapBookingRow(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] getBookingByIdAndUserId error: " + e.getMessage());
        }
        for (Booking b : inMemoryBookings) {
            if (b.getBookingId() == id && b.getUserId() != null && b.getUserId() == userId) {
                return b;
            }
        }
        return null;
    }

    public List<Integer> getBookedSeats(int busId) {
        List<Integer> booked = new ArrayList<>();
        String query = "SELECT seat_number FROM booking WHERE bus_id = ? AND status = 'CONFIRMED'";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                for (Booking b : inMemoryBookings) {
                    if (b.getBusId() == busId && "CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                        booked.add(b.getSeatNumber());
                    }
                }
                return booked;
            }
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, busId);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        booked.add(rs.getInt("seat_number"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] getBookedSeats error: " + e.getMessage());
        }
        if (booked.isEmpty()) {
            for (Booking b : inMemoryBookings) {
                if (b.getBusId() == busId && "CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                    booked.add(b.getSeatNumber());
                }
            }
        }
        return booked;
    }

    /**
     * Retrieves occupied seat numbers mapped to the occupying passenger's gender ('Male', 'Female', 'Other').
     */
    public Map<Integer, String> getBookedSeatsWithGender(int busId) {
        Map<Integer, String> seatGenderMap = new HashMap<>();
        String query = "SELECT b.seat_number, COALESCE(p.gender, 'Other') AS gender " +
                       "FROM booking b " +
                       "LEFT JOIN passenger p ON b.passenger_id = p.passenger_id " +
                       "WHERE b.bus_id = ? AND b.status = 'CONFIRMED'";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                for (Booking b : inMemoryBookings) {
                    if (b.getBusId() == busId && "CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                        String g = b.getPassengerGender();
                        if (g == null || g.trim().isEmpty()) g = "Other";
                        seatGenderMap.put(b.getSeatNumber(), g.trim());
                    }
                }
                return seatGenderMap;
            }
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, busId);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        int seat = rs.getInt("seat_number");
                        String gender = rs.getString("gender");
                        if (gender == null || gender.trim().isEmpty()) {
                            gender = "Other";
                        }
                        seatGenderMap.put(seat, gender.trim());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] getBookedSeatsWithGender error: " + e.getMessage());
        }
        if (seatGenderMap.isEmpty()) {
            for (Booking b : inMemoryBookings) {
                if (b.getBusId() == busId && "CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                    String g = b.getPassengerGender();
                    if (g == null || g.trim().isEmpty()) g = "Other";
                    seatGenderMap.put(b.getSeatNumber(), g.trim());
                }
            }
        }
        return seatGenderMap;
    }

    public boolean isSeatBooked(int busId, int seatNumber) {
        String query = "SELECT COUNT(*) FROM booking WHERE bus_id = ? AND seat_number = ? AND status = 'CONFIRMED'";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                for (Booking b : inMemoryBookings) {
                    if (b.getBusId() == busId && b.getSeatNumber() == seatNumber && "CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                        return true;
                    }
                }
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, busId);
                pst.setInt(2, seatNumber);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] isSeatBooked error: " + e.getMessage());
        }
        for (Booking b : inMemoryBookings) {
            if (b.getBusId() == busId && b.getSeatNumber() == seatNumber && "CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                return true;
            }
        }
        return false;
    }

    public boolean cancelBooking(int bookingId) {
        String query = "UPDATE booking SET status = 'CANCELLED' WHERE booking_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                for (Booking b : inMemoryBookings) {
                    if (b.getBookingId() == bookingId) {
                        b.setStatus("CANCELLED");
                        return true;
                    }
                }
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, bookingId);
                boolean ok = pst.executeUpdate() > 0;
                if (ok) {
                    for (Booking b : inMemoryBookings) {
                        if (b.getBookingId() == bookingId) b.setStatus("CANCELLED");
                    }
                }
                return ok;
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] cancelBooking error: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelBookingForUser(int bookingId, int userId) {
        String query = "UPDATE booking SET status = 'CANCELLED' WHERE booking_id = ? AND user_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                for (Booking b : inMemoryBookings) {
                    if (b.getBookingId() == bookingId && b.getUserId() != null && b.getUserId() == userId) {
                        b.setStatus("CANCELLED");
                        return true;
                    }
                }
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, bookingId);
                pst.setInt(2, userId);
                boolean ok = pst.executeUpdate() > 0;
                if (ok) {
                    for (Booking b : inMemoryBookings) {
                        if (b.getBookingId() == bookingId && b.getUserId() != null && b.getUserId() == userId) {
                            b.setStatus("CANCELLED");
                        }
                    }
                }
                return ok;
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] cancelBookingForUser error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteBooking(int bookingId) {
        String query = "DELETE FROM booking WHERE booking_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return inMemoryBookings.removeIf(b -> b.getBookingId() == bookingId);
            }
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, bookingId);
                boolean ok = pst.executeUpdate() > 0;
                if (ok) {
                    inMemoryBookings.removeIf(b -> b.getBookingId() == bookingId);
                }
                return ok;
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] deleteBooking error: " + e.getMessage());
            return false;
        }
    }
}
