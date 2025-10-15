package DAO;

import DTO.Vehicle;
import java.sql.*;
import mylib.DBUtils;

public class VehicleDAO {

    // ✅ 1️⃣ Thêm phương tiện mới vào DB
    public boolean insertVehicle(Vehicle v) throws ClassNotFoundException {
        String sql = "INSERT INTO Vehicle (User_ID, Model_ID, Vin, License_Plate) VALUES (?, ?, ?, ?)";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, v.getUser_ID());
            ps.setInt(2, v.getModel_ID());
            ps.setString(3, v.getVin());
            ps.setString(4, v.getLicense_Plate());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERROR] insertVehicle: " + e.getMessage());
            return false;
        }
    }

    // ✅ 2️⃣ Lấy Vehicle theo User_ID (JOIN với Vehicle_Model)
    public Vehicle getVehicleByUserId(int userId) throws ClassNotFoundException, SQLException {
        String sql = "SELECT v.Vehicle_ID, v.User_ID, v.Model_ID, v.Vin, v.License_Plate,\n"
                + "                   m.Model_Name, m.Brand, m.Battery_Type\n"
                + "            FROM Vehicle v\n"
                + "            JOIN Vehicle_Model m ON v.Model_ID = m.Model_ID\n"
                + "            WHERE v.User_ID = ?";

        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Vehicle v = new Vehicle();
                    v.setVehicle_ID(rs.getInt("Vehicle_ID"));
                    v.setUser_ID(rs.getInt("User_ID"));
                    v.setModel_ID(rs.getInt("Model_ID"));
                    v.setVin(rs.getString("Vin"));
                    v.setLicense_Plate(rs.getString("License_Plate"));
                    v.setModel_Name(rs.getString("Model_Name"));
                    v.setBrand(rs.getString("Brand"));
                    v.setBattery_Type(rs.getString("Battery_Type"));
                    return v;
                }
            }
        }
        return null;
    }

    // ✅ 3️⃣ Lấy Model_ID theo tên model (dành cho OCR)
    public Integer getModelIdByName(String modelName) throws ClassNotFoundException {
        String sql = "SELECT Model_ID FROM Vehicle_Model WHERE LOWER(Model_Name) = LOWER(?)";

        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, modelName.trim());
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Model_ID");
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] getModelIdByName: " + e.getMessage());
        }
        return null;
    }

    // ✅ 4️⃣ Kiểm tra xem VIN đã tồn tại hay chưa
    public boolean isVinExists(String vin) throws ClassNotFoundException {
        String sql = "SELECT 1 FROM Vehicle WHERE Vin = ?";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, vin);
            try ( ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true nếu đã tồn tại
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] isVinExists: " + e.getMessage());
        }
        return false;
    }

    // ✅ 5️⃣ Xóa phương tiện theo Vehicle_ID (nếu cần reset hoặc unlink)
    public boolean deleteVehicleById(int vehicleId) throws ClassNotFoundException {
        String sql = "DELETE FROM Vehicle WHERE Vehicle_ID = ?";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vehicleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERROR] deleteVehicleById: " + e.getMessage());
        }
        return false;
    }
}
