package DAO;

import DTO.Battery;
import mylib.DBUtils;

import java.sql.*;

public class BatteryDAO {

    public Battery getBatteryById(int id) {
        String sql = "SELECT Battery_ID, Serial_Number, Resistance, SoH, Type_ID FROM dbo.Battery WHERE Battery_ID = ?";
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public int insertBattery(Battery b) {
        String sql = "INSERT INTO dbo.Battery (Serial_Number, Resistance, SoH, Type_ID) " +
                     "VALUES (?, ?, ?, ?)";
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getSerialNumber());
            ps.setDouble(2, b.getResistance());
            ps.setDouble(3, b.getSoH());
            ps.setInt(4, b.getTypeId());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    public boolean deleteBattery(int id) {
        String sql = "DELETE FROM dbo.Battery WHERE Battery_ID = ?";
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private Battery mapRow(ResultSet rs) throws SQLException {
        Battery b = new Battery();
        b.setBatteryId(rs.getInt("Battery_ID"));
        b.setSerialNumber(rs.getString("Serial_Number"));
        b.setResistance(rs.getDouble("Resistance"));
        b.setSoH(rs.getDouble("SoH"));
        b.setTypeId(rs.getInt("Type_ID"));
        return b;
    }
}
