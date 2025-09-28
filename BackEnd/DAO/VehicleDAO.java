/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import DTO.Vehicle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import mylib.DBUtils;

/**
 *
 * @author Surface
 */
public class VehicleDAO {

    public boolean insertVehicle(Vehicle v) throws ClassNotFoundException {
        String sql = "INSERT INTO Vehicle(User_ID, Model, Vin, License_Plate, Battery_Type_Current) "
                + "VALUES (?, ?, ?, ?, ?)";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, v.getUser_ID());
            ps.setString(2, v.getModel());
            ps.setString(3, v.getVin());
            ps.setString(4, v.getLicense_Plate());
            ps.setString(5, v.getBattery_Type_Current());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Vehicle getVehicleByUserId(int userId) throws ClassNotFoundException, SQLException {
        String sql = "SELECT * FROM Vehicle WHERE User_ID = ?";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Vehicle v = new Vehicle();
                v.setVehicle_ID(rs.getInt("Vehicle_ID"));
                v.setUser_ID(rs.getInt("User_ID"));
                v.setModel(rs.getString("Model"));
                v.setVin(rs.getString("Vin"));
                v.setLicense_Plate(rs.getString("License_Plate"));
                v.setBattery_Type_Current(rs.getString("Battery_Type_Current"));
                return v;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
