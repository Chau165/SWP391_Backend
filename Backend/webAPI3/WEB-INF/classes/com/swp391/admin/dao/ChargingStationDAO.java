package com.swp391.admin.dao;

import com.swp391.admin.model.ChargingStation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO for Charging_Station
public class ChargingStationDAO {
    public List<ChargingStation> getByStationId(int stationId) throws SQLException {
        List<ChargingStation> list = new ArrayList<>();
        String sql = "SELECT ChargingStation_ID, Station_ID, Name, Slot_Capacity, Slot_Type, Power_Rating FROM Charging_Station WHERE Station_ID = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ChargingStation(rs.getInt("ChargingStation_ID"), rs.getInt("Station_ID"), rs.getString("Name"), rs.getInt("Slot_Capacity"), rs.getString("Slot_Type"), rs.getString("Power_Rating")));
                }
            }
        }
        return list;
    }

    public ChargingStation getById(int id) throws SQLException {
        String sql = "SELECT ChargingStation_ID, Station_ID, Name, Slot_Capacity, Slot_Type, Power_Rating FROM Charging_Station WHERE ChargingStation_ID = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new ChargingStation(rs.getInt("ChargingStation_ID"), rs.getInt("Station_ID"), rs.getString("Name"), rs.getInt("Slot_Capacity"), rs.getString("Slot_Type"), rs.getString("Power_Rating"));
            }
        }
        return null;
    }

    public boolean insert(ChargingStation cs) throws SQLException {
        String sql = "INSERT INTO Charging_Station(Station_ID, Name, Slot_Capacity, Slot_Type, Power_Rating) VALUES(?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cs.getStationId());
            ps.setString(2, cs.getName());
            ps.setInt(3, cs.getSlotCapacity());
            ps.setString(4, cs.getSlotType());
            ps.setString(5, cs.getPowerRating());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(ChargingStation cs) throws SQLException {
        String sql = "UPDATE Charging_Station SET Name=?, Slot_Capacity=?, Slot_Type=?, Power_Rating=? WHERE ChargingStation_ID=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cs.getName());
            ps.setInt(2, cs.getSlotCapacity());
            ps.setString(3, cs.getSlotType());
            ps.setString(4, cs.getPowerRating());
            ps.setInt(5, cs.getChargingStationId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Charging_Station WHERE ChargingStation_ID = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
