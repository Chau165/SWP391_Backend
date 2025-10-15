package DAO;

import DTO.Package;
import mylib.DBUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PackageDAO {

    // ===== Helper: map 1 row ResultSet -> Package DTO =====
    private Package mapRow(ResultSet rs) throws SQLException {
        Package pkg = new Package();
        // LƯU Ý: tên setter phải khớp DTO của bạn
        // (bạn đang dùng setPackageId / setRequiredSoH ...)
        pkg.setPackageId(rs.getInt("Package_ID"));
        pkg.setName(rs.getString("Name"));
        pkg.setDescription(rs.getString("Description"));
        pkg.setPrice(rs.getDouble("Price"));
        pkg.setRequiredSoH(rs.getDouble("Required_SoH"));
        pkg.setMinSoH(rs.getInt("MinSoH"));
        pkg.setMaxSoH(rs.getInt("MaxSoH"));
        return pkg;
    }

    // ===== 1) Lấy toàn bộ package (tự mở connection) =====
    public List<Package> getAllPackage() {
        List<Package> list = new ArrayList<>();
        String sql = "SELECT Package_ID, Name, Description, Price, Required_SoH, MinSoH, MaxSoH FROM dbo.[Package]";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ===== 1a) Overload: Lấy toàn bộ package nhưng dùng connection có sẵn =====
    public List<Package> getAllPackage(Connection con) throws SQLException {
        List<Package> list = new ArrayList<>();
        String sql = "SELECT Package_ID, Name, Description, Price, Required_SoH, MinSoH, MaxSoH FROM dbo.[Package]";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ===== 2) Lấy package theo ID (tự mở connection) =====
    public Package getPackageById(int id) {
        String query = "SELECT Package_ID, Name, Description, Price, Required_SoH, MinSoH, MaxSoH " +
                       "FROM dbo.[Package] WHERE Package_ID = ?";
        try (Connection connect = DBUtils.getConnection();
             PreparedStatement ps = connect.prepareStatement(query)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs != null && rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===== 2a) Overload: Lấy package theo ID dùng connection có sẵn =====
    public Package getPackageById(Connection con, int id) throws SQLException {
        String query = "SELECT Package_ID, Name, Description, Price, Required_SoH, MinSoH, MaxSoH " +
                       "FROM dbo.[Package] WHERE Package_ID = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return (rs != null && rs.next()) ? mapRow(rs) : null;
            }
        }
    }

    // ===== 3) Tiện ích: Lấy package theo User_ID (qua Users.Package_ID) =====
    //   -> Dùng cho booking flow: lấy gói của user hiện tại.
    public Package getPackageByUserId(Connection con, int userId) throws SQLException {
        String sql = "SELECT p.Package_ID, p.Name, p.Description, p.Price, p.Required_SoH, p.MinSoH, p.MaxSoH " +
                     "FROM dbo.Users u " +
                     "JOIN dbo.[Package] p ON u.Package_ID = p.Package_ID " +
                     "WHERE u.ID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return (rs != null && rs.next()) ? mapRow(rs) : null;
            }
        }
    }
}
