package DAO;

import DTO.DriverPackage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import mylib.DBUtils;

public class DriverPackageDAO {

    public boolean insertDriverPackage(DriverPackage dp) throws ClassNotFoundException {
        String sql = "INSERT INTO DriverPackage(User_ID, Package_ID, Start_date, End_date) "
                + "VALUES (?, ?, ?, ?)";
        try ( Connection connect = DBUtils.getConnection();  
                PreparedStatement ps = connect.prepareStatement(sql)) {
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

    public boolean updateDriverPackage(DriverPackage dp) throws ClassNotFoundException {
        String sql = "UPDATE DriverPackage SET Package_ID = ?, Start_date = ?, End_date = ? WHERE User_ID = ?";
        try ( Connection connect = DBUtils.getConnection();  
                PreparedStatement ps = connect.prepareStatement(sql)) {
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
    
     public boolean existsDriverPackage(int userId) throws ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM DriverPackage WHERE User_ID = ?";
        try (Connection connect = DBUtils.getConnection();
             PreparedStatement ps = connect.prepareStatement(sql)) {
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
}
