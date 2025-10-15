package DAO;

import DTO.DriverPackage;
import DTO.Package;
import mylib.DBUtils;

import java.sql.*;

public class DriverPackageDAO {

    public Integer getCurrentPackageId(Connection con, int userId) throws SQLException {
        String sql
                = "SELECT TOP 1 Package_ID "
                + "FROM dbo.DriverPackage "
                + "WHERE User_ID=? AND (End_date IS NULL OR End_date >= CAST(GETDATE() AS DATE)) "
                + "ORDER BY "
                + "  CASE WHEN End_date IS NULL THEN 1 ELSE 0 END DESC, "
                + "  End_date DESC";
        try ( PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try ( ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public Package getCurrentPackage(Connection con, int userId) throws SQLException {
        String sql
                = "SELECT TOP 1 p.Package_ID, p.Name, p.Description, p.Price, p.Required_SoH, p.MinSoH, p.MaxSoH "
                + "FROM dbo.DriverPackage dp "
                + "JOIN dbo.[Package] p ON p.Package_ID = dp.Package_ID "
                + "WHERE dp.User_ID=? AND (dp.End_date IS NULL OR dp.End_date >= CAST(GETDATE() AS DATE)) "
                + "ORDER BY "
                + "  CASE WHEN dp.End_date IS NULL THEN 1 ELSE 0 END DESC, "
                + "  dp.End_date DESC";
        try ( PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try ( ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Package pkg = new Package();
                pkg.setPackageId(rs.getInt("Package_ID"));
                pkg.setName(rs.getString("Name"));
                pkg.setDescription(rs.getString("Description"));
                pkg.setPrice(rs.getDouble("Price"));
                pkg.setRequiredSoH(rs.getDouble("Required_SoH"));
                pkg.setMinSoH(rs.getInt("MinSoH"));
                pkg.setMaxSoH(rs.getInt("MaxSoH"));
                return pkg;
            }
        }
    }

    public boolean existsDriverPackage(int userId) throws ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM DriverPackage WHERE User_ID = ?";
        try ( Connection connect = DBUtils.getConnection();  PreparedStatement ps = connect.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateDriverPackage(DriverPackage dp) throws ClassNotFoundException {
        String sql = "UPDATE DriverPackage SET Package_ID = ?, Start_date = ?, End_date = ? WHERE User_ID = ?";
        try ( Connection connect = DBUtils.getConnection();  PreparedStatement ps = connect.prepareStatement(sql)) {
            ps.setInt(1, dp.getPackage_ID());
            ps.setDate(2, dp.getStart_date());
            ps.setDate(3, dp.getEnd_date());
            ps.setInt(4, dp.getUser_ID());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
     public boolean insertDriverPackage(DriverPackage dp) throws ClassNotFoundException {
        String sql = "INSERT INTO DriverPackage(User_ID, Package_ID, Start_date, End_date) "
                + "VALUES (?, ?, ?, ?)";
        try ( Connection connect = DBUtils.getConnection();  PreparedStatement ps = connect.prepareStatement(sql)) {
            ps.setInt(1, dp.getUser_ID());
            ps.setInt(2, dp.getPackage_ID());
            ps.setDate(3, dp.getStart_date());
            ps.setDate(4, dp.getEnd_date());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
