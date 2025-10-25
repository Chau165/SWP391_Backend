package DAO;

import DTO.Battery;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mylib.DBUtils;

public class BatteryDAO {
    public List<Battery> getAll() {
        List<Battery> list = new ArrayList<>();
        String sql = "SELECT ID, Serial, Status, Station_ID FROM Battery";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Battery(rs.getInt("ID"), rs.getString("Serial"), rs.getString("Status"), rs.getObject("Station_ID") != null ? rs.getInt("Station_ID") : null));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public Battery getById(int id) {
        String sql = "SELECT ID, Serial, Status, Station_ID FROM Battery WHERE ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Battery(rs.getInt("ID"), rs.getString("Serial"), rs.getString("Status"), rs.getObject("Station_ID") != null ? rs.getInt("Station_ID") : null);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean create(Battery b) {
        String sql = "INSERT INTO Battery (Serial, Status, Station_ID) VALUES (?, ?, ?)";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getSerial());
            ps.setString(2, b.getStatus());
            if (b.getStationId() == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, b.getStationId());
            int r = ps.executeUpdate();
            return r > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean update(Battery b) {
        String sql = "UPDATE Battery SET Serial = ?, Status = ?, Station_ID = ? WHERE ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getSerial());
            ps.setString(2, b.getStatus());
            if (b.getStationId() == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, b.getStationId());
            ps.setInt(4, b.getId());
            int r = ps.executeUpdate();
            return r > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM Battery WHERE ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int r = ps.executeUpdate();
            return r > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}
