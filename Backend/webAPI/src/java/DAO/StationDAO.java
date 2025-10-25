/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import DTO.Station;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mylib.DBUtils;

/**
 *
 * @author Surface
 */
public class StationDAO {

    public List<Station> getAllStation() {
        List<Station> list = new ArrayList<>();
    String sql = "SELECT Station_ID, Name, Address FROM Station";

        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Station(
                        rs.getInt("Station_ID"),
                        rs.getString("Name"),
                        rs.getString("Address")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Station> searchStation(String search) {
        List<Station> list = new ArrayList<>();
        String query = "SELECT Station_ID, Name, Address \n"
                + "FROM Station\n"
                + "WHERE Address LIKE ? OR Name LIKE ?;";
        try ( Connection connect = DBUtils.getConnection();  PreparedStatement ps = connect.prepareStatement(query)) {
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Station station = new Station();
                station.setStation_ID(rs.getInt("Station_ID"));
                station.setName(rs.getString("Name"));
                station.setAddress(rs.getString("Address"));
                // Total_Battery column removed from Station table; nothing to set here
                list.add(station);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Station> getStationsByUserId(int userId) {
        List<Station> list = new ArrayList<>();
        String sql = "SELECT DISTINCT st.Station_ID, st.Name, st.Address "
                + "FROM swap_transactions s JOIN Station st ON s.Station_ID = st.Station_ID "
                + "WHERE s.User_ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Station station = new Station(
                            rs.getInt("Station_ID"),
                            rs.getString("Name"),
                            rs.getString("Address")
                    );
                    list.add(station);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- New convenience CRUD methods expected by controllers ---
    public Station getStationById(int id) {
        String sql = "SELECT Station_ID, Name, Address FROM Station WHERE Station_ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Station(rs.getInt("Station_ID"), rs.getString("Name"), rs.getString("Address"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Compatibility shim: some controllers call getAll(q). Provide that
     * method and dispatch to getAllStation() or searchStation(search).
     */
    public List<Station> getAll(String q) {
        if (q == null || q.trim().isEmpty()) return getAllStation();
        return searchStation(q);
    }

    public int insertStation(Station s) {
        String sql = "INSERT INTO Station (Name, Address) VALUES (?, ?)";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getAddress());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
                return 1; // fallback: return positive id unknown
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateStation(Station s) {
        String sql = "UPDATE Station SET Name = ?, Address = ? WHERE Station_ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getAddress());
            ps.setInt(3, s.getStation_ID());
            int r = ps.executeUpdate();
            return r > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteStation(int id) {
        String sql = "DELETE FROM Station WHERE Station_ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int r = ps.executeUpdate();
            return r > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // aliases used by other controllers
    public boolean createStation(Station s) { return insertStation(s) > 0; }
    // updateStation already defined above
    // deleteStation already defined above

    public java.util.Map<String,Object> getAnalytics() {
        java.util.Map<String,Object> m = new java.util.HashMap<>();
        try (Connection conn = DBUtils.getConnection()) {
            // station count
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Station"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) m.put("stationCount", rs.getInt(1)); else m.put("stationCount", 0);
            }
            // charging station count
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Charging_Station"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) m.put("chargingStationCount", rs.getInt(1)); else m.put("chargingStationCount", 0);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return m;
    }
}
