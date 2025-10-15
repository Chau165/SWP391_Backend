package DAO;

import DTO.Booking;
import java.sql.*;

public class BookingDAO {

    public int insertBooking(Connection con, Booking b) throws SQLException {
        String sql =
            "INSERT INTO dbo.Booking (" +
            "  User_ID, Vehicle_ID, Package_ID," +
            "  Station_ID, ChargingStation_ID, Slot_ID," +
            "  Battery_Request, Status, Booking_Time, Expired_Date, Qr_Code" +
            ") OUTPUT INSERTED.Booking_ID " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            ps.setInt(i++, b.getUser_ID());
            ps.setInt(i++, b.getVehicle_ID());
            ps.setInt(i++, b.getPackage_ID());
            ps.setInt(i++, b.getStation_ID());
            ps.setInt(i++, b.getChargingStation_ID());
            ps.setInt(i++, b.getSlot_ID());
            ps.setString(i++, b.getBattery_Request());
            ps.setString(i++, b.getStatus());
            ps.setTimestamp(i++, b.getBooking_Time());
            ps.setTimestamp(i++, b.getExpired_Date());
            ps.setString(i++, b.getQr_Code());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public void updateQRCode(Connection con, int bookingId, String base64) throws SQLException {
        String sql = "UPDATE dbo.Booking SET Qr_Code=? WHERE Booking_ID=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, base64);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    public int getVehicleIdByUserId(Connection con, int userId) throws SQLException {
        String sql = "SELECT TOP 1 Vehicle_ID FROM dbo.Vehicle WHERE User_ID=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public int getStationIdByName(Connection con, String stationName) throws SQLException {
        String sql = "SELECT Station_ID FROM dbo.Station WHERE Name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, stationName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }
    
        public Booking getBookingById(int id) {
        String sql = "SELECT Booking_ID, Vehicle_ID, Battery_Request, Package_ID, User_ID, Station_ID, Status, Qr_Code, " +
                     "Booking_Time, Expired_Date, ChargingStation_ID, Slot_ID " +
                     "FROM dbo.Booking WHERE Booking_ID = ?";
        try (Connection con = mylib.DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean updateStatus(int bookingId, String status) {
        String sql = "UPDATE dbo.Booking SET Status=? WHERE Booking_ID=?";
        try (Connection con = mylib.DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate()>0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBooking_ID(rs.getInt("Booking_ID"));
        b.setVehicle_ID(rs.getInt("Vehicle_ID"));
        b.setBattery_Request(rs.getString("Battery_Request"));
        b.setPackage_ID(rs.getInt("Package_ID"));
        b.setUser_ID(rs.getInt("User_ID"));
        b.setStation_ID(rs.getInt("Station_ID"));
        b.setStatus(rs.getString("Status"));
        b.setQr_Code(rs.getString("Qr_Code"));
        b.setBooking_Time(rs.getTimestamp("Booking_Time"));
        b.setExpired_Date(rs.getTimestamp("Expired_Date"));
        b.setChargingStation_ID(rs.getInt("ChargingStation_ID"));
        int slot = rs.getInt("Slot_ID");
        if (rs.wasNull()) slot = 0;
        b.setSlot_ID(slot);
        return b;
    }
}
