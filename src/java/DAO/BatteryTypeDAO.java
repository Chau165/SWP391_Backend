package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import mylib.DBUtils;

public class BatteryTypeDAO {

    // Lấy ID từ model (tên model lưu trong cột Model)
    public int getBatteryTypeIdByName(String name) {
        final String sql
                = "SELECT ID "
                + "FROM dbo.Battery_Type "
                + "WHERE LTRIM(RTRIM([Model])) COLLATE SQL_Latin1_General_CP1_CI_AS = "
                + "      LTRIM(RTRIM(?)) COLLATE SQL_Latin1_General_CP1_CI_AS";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name == null ? null : name.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                }
            }

        } catch (SQLException e) {
            System.err.println("[BatteryTypeDAO.getBatteryTypeIdByName] SQL error: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("[BatteryTypeDAO.getBatteryTypeIdByName] Driver error: " + e.getMessage());
            e.printStackTrace();
        }

        return -1; // không tìm thấy hoặc lỗi
    }

    // Lấy model (tên) từ ID
    public String getBatteryTypeNameById(int id) {
        final String sql = "SELECT Model FROM dbo.Battery_Type WHERE ID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Model");
                }
            }

        } catch (SQLException e) {
            System.err.println("[BatteryTypeDAO.getBatteryTypeNameById] SQL error: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("[BatteryTypeDAO.getBatteryTypeNameById] Driver error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
