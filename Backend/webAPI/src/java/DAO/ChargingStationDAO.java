package DAO;

import DTO.ChargingStation;
import mylib.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChargingStationDAO {

    public List<ChargingStation> getByStationId(int stationId) throws Exception {
        List<ChargingStation> list = new ArrayList<>();
        String sql = "SELECT ChargingStation_ID, Station_ID, Name, Slot_Capacity, Slot_Type, Power_Rating FROM Charging_Station WHERE Station_ID = ?";
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
                // Power_Rating may be stored as a string like "5.0 kW" in DB; extract numeric part
                String powerRaw = null;
                try { powerRaw = rs.getString("Power_RATING"); } catch(Exception _ex){ powerRaw = null; }
                double pr = 0.0;
                if (powerRaw != null) {
                    String m = (powerRaw.replaceAll("[^0-9.\\-]",""));
                    if (m != null && m.length() > 0) {
                        try { pr = Double.parseDouble(m); } catch(Exception __) { pr = 0.0; }
                    }
                }
                c.setPowerRating(pr);
                list.add(c);
            }
        }
        return list;
    }

    public ChargingStation getById(int id) throws Exception {
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
                String powerRaw = null;
                try { powerRaw = rs.getString("Power_RATING"); } catch(Exception _ex){ powerRaw = null; }
                double pr = 0.0;
                if (powerRaw != null) {
                    String m = (powerRaw.replaceAll("[^0-9.\\-]",""));
                    if (m != null && m.length() > 0) {
                        try { pr = Double.parseDouble(m); } catch(Exception __) { pr = 0.0; }
                    }
                }
                c.setPowerRating(pr);
                return c;
            }
        }
        return null;
    }

    public boolean insert(ChargingStation c) throws Exception {
        String sql = "INSERT INTO Charging_Station (Station_ID, Name, Slot_Capacity, Slot_Type, Power_RATING) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getStationId());
            ps.setString(2, c.getName());
            ps.setInt(3, c.getSlotCapacity());
            ps.setString(4, c.getSlotType());
            // store as formatted string with unit, e.g. "5.0 kW"
            String powerStr = String.format("%.1f kW", c.getPowerRating());
            ps.setString(5, powerStr);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(ChargingStation c) throws Exception {
        String sql = "UPDATE Charging_Station SET Name = ?, Slot_Capacity = ?, Slot_Type = ?, Power_RATING = ? WHERE ChargingStation_ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setInt(2, c.getSlotCapacity());
            ps.setString(3, c.getSlotType());
            String powerStr = String.format("%.1f kW", c.getPowerRating());
            ps.setString(4, powerStr);
            ps.setInt(5, c.getChargingStationId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM Charging_Station WHERE ChargingStation_ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
