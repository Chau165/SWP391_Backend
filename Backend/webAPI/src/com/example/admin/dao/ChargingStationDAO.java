package DAO;

import DTO.ChargingStation;
import mylib.DBUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChargingStationDAO {

    public List<ChargingStation> getByStationId(int stationId) {
        List<ChargingStation> list = new ArrayList<>();
        String sql = "SELECT ChargingStation_ID, Station_ID, Name, Slot_Capacity, Slot_Type, Power_RATING FROM Charging_Station WHERE Station_ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChargingStation c = new ChargingStation();
                c.setChargingStationId(rs.getInt("ChargingStation_ID"));
                c.setStationId(rs.getInt("Station_ID"));
                c.setName(rs.getString("Name"));
                c.setSlotCapacity(rs.getInt("Slot_Capacity"));
                c.setSlotType(rs.getString("Slot_Type"));
                c.setPowerRating(rs.getDouble("Power_RATING"));
                list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ChargingStation getById(int id) {
        String sql = "SELECT ChargingStation_ID, Station_ID, Name, Slot_Capacity, Slot_Type, Power_RATING FROM Charging_Station WHERE ChargingStation_ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ChargingStation c = new ChargingStation();
                c.setChargingStationId(rs.getInt("ChargingStation_ID"));
                c.setStationId(rs.getInt("Station_ID"));
                c.setName(rs.getString("Name"));
                c.setSlotCapacity(rs.getInt("Slot_Capacity"));
                c.setSlotType(rs.getString("Slot_Type"));
                c.setPowerRating(rs.getDouble("Power_RATING"));
                return c;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(ChargingStation c) {
        String sql = "INSERT INTO Charging_Station (Station_ID, Name, Slot_Capacity, Slot_Type, Power_RATING) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getStationId());
            ps.setString(2, c.getName());
            ps.setInt(3, c.getSlotCapacity());
            ps.setString(4, c.getSlotType());
            ps.setDouble(5, c.getPowerRating());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(ChargingStation c) {
        String sql = "UPDATE Charging_Station SET Name = ?, Slot_Capacity = ?, Slot_Type = ?, Power_RATING = ? WHERE ChargingStation_ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setInt(2, c.getSlotCapacity());
            ps.setString(3, c.getSlotType());
            ps.setDouble(4, c.getPowerRating());
            ps.setInt(5, c.getChargingStationId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM Charging_Station WHERE ChargingStation_ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
