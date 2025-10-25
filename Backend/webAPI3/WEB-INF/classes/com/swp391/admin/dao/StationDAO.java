package com.swp391.admin.dao;

import com.swp391.admin.model.Station;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO for Station table
public class StationDAO {
    public List<Station> getAll() throws SQLException {
        List<Station> list = new ArrayList<>();
        String sql = "SELECT Station_ID, Name, Address FROM Station";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Station(rs.getInt("Station_ID"), rs.getString("Name"), rs.getString("Address")));
            }
        }
        return list;
    }

    public Station getById(int id) throws SQLException {
        String sql = "SELECT Station_ID, Name, Address FROM Station WHERE Station_ID = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Station(rs.getInt("Station_ID"), rs.getString("Name"), rs.getString("Address"));
            }
        }
        return null;
    }

    public boolean insert(Station s) throws SQLException {
        String sql = "INSERT INTO Station(Name, Address) VALUES(?,?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getAddress());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Station s) throws SQLException {
        String sql = "UPDATE Station SET Name = ?, Address = ? WHERE Station_ID = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getAddress());
            ps.setInt(3, s.getStationId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Station WHERE Station_ID = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
