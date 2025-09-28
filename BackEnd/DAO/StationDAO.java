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
        String sql = "SELECT Station_ID, Name, Address, Total_Battery FROM Station";

        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Station(
                        rs.getInt("Station_ID"),
                        rs.getString("Name"),
                        rs.getString("Address"),
                        rs.getInt("Total_Battery")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Station> searchStation(String search) {
        List<Station> list = new ArrayList<>();
        String query = "SELECT Station_ID, Name, Address, Total_Battery \n"
                + "FROM Station\n"
                + "WHERE Address LIKE ? OR Name LIKE ?;";
        try ( Connection connect = DBUtils.getConnection();  PreparedStatement ps = connect.prepareStatement(query)) {
            ps.setString(1, "%" + search + "%");
            ps.setString(1, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Station station = new Station();
                station.setStation_ID(rs.getInt("Station_ID"));
                station.setName(rs.getString("Name"));
                station.setAddress(rs.getString("Address"));
                station.setTotal_Battery(rs.getInt("Total_Battery"));
                list.add(station);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
