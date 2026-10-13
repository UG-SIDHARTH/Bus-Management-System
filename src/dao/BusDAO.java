package dao;

import database.DBConnection;
import model.Bus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BusDAO {

    public boolean addBus(Bus b) {
        String query = "INSERT INTO bus (bus_id, bus_name, source, destination, total_seats, fare) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return false;
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, b.getBusId());
                pst.setString(2, b.getBusName());
                pst.setString(3, b.getSource());
                pst.setString(4, b.getDestination());
                pst.setInt(5, b.getTotalSeats());
                pst.setDouble(6, b.getFare());
                return pst.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Bus> getAllBuses() {
        List<Bus> list = new ArrayList<>();
        String query = "SELECT * FROM bus ORDER BY bus_id ASC";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return list;
            try (PreparedStatement pst = con.prepareStatement(query);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Bus b = new Bus(
                            rs.getInt("bus_id"),
                            rs.getString("bus_name"),
                            rs.getString("source"),
                            rs.getString("destination"),
                            rs.getInt("total_seats"),
                            rs.getDouble("fare")
                    );
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Bus getBusById(int id) {
        String query = "SELECT * FROM bus WHERE bus_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return null;
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, id);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return new Bus(
                                rs.getInt("bus_id"),
                                rs.getString("bus_name"),
                                rs.getString("source"),
                                rs.getString("destination"),
                                rs.getInt("total_seats"),
                                rs.getDouble("fare")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateBus(Bus b) {
        String query = "UPDATE bus SET bus_name = ?, source = ?, destination = ?, total_seats = ?, fare = ? WHERE bus_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return false;
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setString(1, b.getBusName());
                pst.setString(2, b.getSource());
                pst.setString(3, b.getDestination());
                pst.setInt(4, b.getTotalSeats());
                pst.setDouble(5, b.getFare());
                pst.setInt(6, b.getBusId());
                return pst.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBus(int id) {
        String query = "DELETE FROM bus WHERE bus_id = ?";
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
