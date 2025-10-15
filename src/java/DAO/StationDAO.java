package DAO;

import DTO.Station;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mylib.DBUtils;

public class StationDAO {

    // Lấy tất cả trạm
    public List<Station> getAllStation() {
        List<Station> list = new ArrayList<>();
        String sql = "SELECT Station_ID, Name, Address FROM Station";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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

    // Tìm trạm theo tên hoặc địa chỉ
    public List<Station> searchStation(String search) {
        List<Station> list = new ArrayList<>();
        String query = "SELECT Station_ID, Name, Address " +
                       "FROM Station " +
                       "WHERE Address LIKE ? OR Name LIKE ?";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            String keyword = "%" + search + "%";
            ps.setString(1, keyword);
            ps.setString(2, keyword);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Station station = new Station();
                    station.setStation_ID(rs.getInt("Station_ID"));
                    station.setName(rs.getString("Name"));
                    station.setAddress(rs.getString("Address"));
                    list.add(station);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy ID từ tên trạm
    public int getStationIdByName(String name) {
        String sql = "SELECT Station_ID FROM Station WHERE Name = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("Station_ID");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Lấy tên trạm từ ID
    public String getStationNameById(int id) {
        String sql = "SELECT Name FROM Station WHERE Station_ID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("Name");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Giả sử trong bảng Station có cột Manager_Email
    public String getManagerEmailById(int id) {
        String sql = "SELECT Manager_Email FROM Station WHERE Station_ID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("Manager_Email");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
