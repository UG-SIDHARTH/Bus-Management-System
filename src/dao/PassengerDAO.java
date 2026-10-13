package dao;

import database.DBConnection;
import model.Passenger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PassengerDAO {

    public static final List<Passenger> SEEDED_FALLBACK_PASSENGERS = List.of(
        new Passenger(101, "John Doe", 28, "Male", "9876543210"),
        new Passenger(102, "Sarah Smith", 24, "Female", "9123456780"),
        new Passenger(103, "Michael Brown", 35, "Male", "9988776655"),
        new Passenger(104, "Robert Williams", 42, "Male", "9811223344", true, "Locomotor / Mobility", "UDID-98765-2026"),
        new Passenger(105, "Elena Rostova", 29, "Female", "9855667788", true, "Visual Impairment", "UDID-55443-2026")
    );

    private static final List<Passenger> inMemoryPassengers = new CopyOnWriteArrayList<>(SEEDED_FALLBACK_PASSENGERS);

    public boolean addPassenger(Passenger p) {
        String query = "INSERT INTO passenger (passenger_id, name, age, gender, phone, is_disability, disability_type, disability_id) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                inMemoryPassengers.add(p);
                return true;
            }
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, p.getPassengerId());
                pst.setString(2, p.getName());
                pst.setInt(3, p.getAge());
                pst.setString(4, p.getGender());
                pst.setString(5, p.getPhoneNumber());
                pst.setBoolean(6, p.isDisability());
                pst.setString(7, p.getDisabilityType());
                pst.setString(8, p.getDisabilityId());

                int rows = pst.executeUpdate();
                if (rows > 0) {
                    if (p.isDisability() && p.getDisabilityId() != null && !p.getDisabilityId().trim().isEmpty()) {
                        saveDisabilityRegistry(con, p.getPassengerId(), p.getDisabilityType(), p.getDisabilityId());
                    }
                    return true;
                }
            } catch (SQLException ex) {
                // Fallback to legacy schema if columns not yet altered
                String legacy = "INSERT INTO passenger (passenger_id, name, age, gender, phone) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pst2 = con.prepareStatement(legacy)) {
                    pst2.setInt(1, p.getPassengerId());
                    pst2.setString(2, p.getName());
                    pst2.setInt(3, p.getAge());
                    pst2.setString(4, p.getGender());
                    pst2.setString(5, p.getPhoneNumber());
                    return pst2.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void saveDisabilityRegistry(Connection con, int passengerId, String type, String certNo) {
        String sql = "INSERT INTO passenger_disability (passenger_id, disability_type, certificate_no, concession_percentage, verified_status) " +
                     "VALUES (?, ?, ?, 50.0, 'VERIFIED') " +
                     "ON DUPLICATE KEY UPDATE disability_type = VALUES(disability_type), certificate_no = VALUES(certificate_no)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, passengerId);
            pst.setString(2, type != null ? type : "General Disability");
            pst.setString(3, certNo);
            pst.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private Passenger mapPassengerRow(ResultSet rs) throws SQLException {
        boolean isDisability = false;
        String disabilityType = null;
        String disabilityId = null;

        try {
            isDisability = rs.getBoolean("is_disability");
            disabilityType = rs.getString("disability_type");
            disabilityId = rs.getString("disability_id");
        } catch (SQLException ignored) {
            // Legacy schema without disability columns
        }

        return new Passenger(
                rs.getInt("passenger_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("gender"),
                rs.getString("phone"),
                isDisability,
                disabilityType,
                disabilityId
        );
    }

    private static final java.util.concurrent.atomic.AtomicInteger nextGeneratedPassengerId = new java.util.concurrent.atomic.AtomicInteger(200);

    public synchronized int getNextPassengerId() {
        String query = "SELECT COALESCE(MAX(passenger_id), 105) + 1 AS next_id FROM passenger";
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement pst = con.prepareStatement(query);
                     ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("next_id");
                    }
                }
            }
        } catch (SQLException ignored) {}
        return nextGeneratedPassengerId.incrementAndGet();
    }

    public Passenger findByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String clean = name.trim().toLowerCase();
        String query = "SELECT * FROM passenger WHERE LOWER(name) = ? LIMIT 1";
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement pst = con.prepareStatement(query)) {
                    pst.setString(1, clean);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            return mapPassengerRow(rs);
                        }
                    }
                }
            }
        } catch (SQLException ignored) {}
        for (Passenger p : inMemoryPassengers) {
            if (p.getName().equalsIgnoreCase(clean)) return p;
        }
        return null;
    }

    public List<Passenger> getAllPassengers() {
        List<Passenger> list = new ArrayList<>();
        String query = "SELECT * FROM passenger ORDER BY passenger_id ASC";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return new ArrayList<>(inMemoryPassengers);
            try (PreparedStatement pst = con.prepareStatement(query);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPassengerRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PassengerDAO] getAllPassengers note: " + e.getMessage());
        }
        return list.isEmpty() ? new ArrayList<>(inMemoryPassengers) : list;
    }

    public Passenger getPassengerById(int id) {
        String query = "SELECT * FROM passenger WHERE passenger_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                for (Passenger p : inMemoryPassengers) {
                    if (p.getPassengerId() == id) return p;
                }
                return null;
            }
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, id);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return mapPassengerRow(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[PassengerDAO] getPassengerById note: " + e.getMessage());
        }
        for (Passenger p : inMemoryPassengers) {
            if (p.getPassengerId() == id) return p;
        }
        return null;
    }

    public boolean updatePassenger(Passenger p) {
        String query = "UPDATE passenger SET name = ?, age = ?, gender = ?, phone = ?, " +
                       "is_disability = ?, disability_type = ?, disability_id = ? " +
                       "WHERE passenger_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                for (int i = 0; i < inMemoryPassengers.size(); i++) {
                    if (inMemoryPassengers.get(i).getPassengerId() == p.getPassengerId()) {
                        inMemoryPassengers.set(i, p);
                        return true;
                    }
                }
                inMemoryPassengers.add(p);
                return true;
            }
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setString(1, p.getName());
                pst.setInt(2, p.getAge());
                pst.setString(3, p.getGender());
                pst.setString(4, p.getPhoneNumber());
                pst.setBoolean(5, p.isDisability());
                pst.setString(6, p.getDisabilityType());
                pst.setString(7, p.getDisabilityId());
                pst.setInt(8, p.getPassengerId());

                int rows = pst.executeUpdate();
                if (rows > 0) {
                    if (p.isDisability() && p.getDisabilityId() != null && !p.getDisabilityId().trim().isEmpty()) {
                        saveDisabilityRegistry(con, p.getPassengerId(), p.getDisabilityType(), p.getDisabilityId());
                    }
                    return true;
                }
            } catch (SQLException ex) {
                // Fallback to legacy update
                String legacy = "UPDATE passenger SET name = ?, age = ?, gender = ?, phone = ? WHERE passenger_id = ?";
                try (PreparedStatement pst2 = con.prepareStatement(legacy)) {
                    pst2.setString(1, p.getName());
                    pst2.setInt(2, p.getAge());
                    pst2.setString(3, p.getGender());
                    pst2.setString(4, p.getPhoneNumber());
                    pst2.setInt(5, p.getPassengerId());
                    return pst2.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deletePassenger(int id) {
        String query = "DELETE FROM passenger WHERE passenger_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return inMemoryPassengers.removeIf(p -> p.getPassengerId() == id);
            }
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, id);
                return pst.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
