package DAO;

import DTO.ChargingStation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import static mylib.DBUtils.getConnection;

public class ChargingStationDAO{

    /**
     * Lấy tất cả charging stations
     * @return 
     */
    public List<ChargingStation> getAllChargingStations() {
        List<ChargingStation> list = new ArrayList<>();
        String sql = "SELECT [ChargingStation_ID], [Station_ID], [Name], [Slot_Capacity], " +
                    "[Slot_Type], [Power_Rating] FROM [Charging_Station]";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                ChargingStation cs = new ChargingStation(
                    rs.getInt("ChargingStation_ID"),
                    rs.getInt("Station_ID"),
                    rs.getString("Name"),
                    rs.getInt("Slot_Capacity"),
                    rs.getString("Slot_Type"),
                    rs.getDouble("Power_Rating")
                );
                list.add(cs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy ChargingStation_ID theo tên trạm
     * @param stationName
     * @return 
     */
    public int getChargingStationIdByName(String stationName) {
        String sql = "SELECT [ChargingStation_ID] FROM [Charging_Station] WHERE [Name] = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, stationName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ChargingStation_ID");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Lấy thông tin charging station theo ID
     * @param chargingStationId
     * @return 
     */
    public ChargingStation getChargingStationById(int chargingStationId) {
        String sql = "SELECT [ChargingStation_ID], [Station_ID], [Name], [Slot_Capacity], " +
                    "[Slot_Type], [Power_Rating] FROM [Charging_Station] WHERE [ChargingStation_ID] = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, chargingStationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ChargingStation(
                        rs.getInt("ChargingStation_ID"),
                        rs.getInt("Station_ID"),
                        rs.getString("Name"),
                        rs.getInt("Slot_Capacity"),
                        rs.getString("Slot_Type"),
                        rs.getDouble("Power_Rating")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}