package dao;

import database.DBConnection;
import model.Passenger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PassengerDAO {

    public boolean addPassenger(Passenger p) {
        String query = "INSERT INTO passenger (passenger_id, name, age, gender, phone, is_disability, disability_type, disability_id) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return false;
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

    public List<Passenger> getAllPassengers() {
        List<Passenger> list = new ArrayList<>();
        String query = "SELECT * FROM passenger ORDER BY passenger_id ASC";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return list;
            try (PreparedStatement pst = con.prepareStatement(query);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPassengerRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Passenger getPassengerById(int id) {
        String query = "SELECT * FROM passenger WHERE passenger_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return null;
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, id);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return mapPassengerRow(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePassenger(Passenger p) {
        String query = "UPDATE passenger SET name = ?, age = ?, gender = ?, phone = ?, " +
                       "is_disability = ?, disability_type = ?, disability_id = ? " +
                       "WHERE passenger_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return false;
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
            if (con == null) return false;
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
