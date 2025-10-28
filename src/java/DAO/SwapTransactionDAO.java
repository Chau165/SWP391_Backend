package DAO;

import DTO.SwapTransaction;
import DTO.PeakHourStatistics;
import mylib.DBUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SwapTransactionDAO {

    // Lấy giao dịch đổi pin gần nhất theo Driver_ID (toàn hệ thống)
    public SwapTransaction getLastSwapByDriverId(int driverId) {
        String sql = "SELECT TOP 1 " +
                "ID, Driver_ID, Staff_ID, Station_ID, ChargingStation_ID, Old_Battery, New_Battery, " +
                "SoH_Old, SoH_New, Fee, Payment_ID, Status, Swap_Time, Booking_ID " +
                "FROM dbo.SwapTransaction WHERE Driver_ID = ? " +
                "ORDER BY Swap_Time DESC";
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ✅ Lấy giao dịch đổi pin gần nhất theo Driver_ID TẠI một ChargingStation_ID cụ thể (tiện cho check-in tại trạm)
    public SwapTransaction getLastSwapByDriverIdAtCS(int driverId, int chargingStationId) {
        String sql = "SELECT TOP 1 " +
                "ID, Driver_ID, Staff_ID, Station_ID, ChargingStation_ID, Old_Battery, New_Battery, " +
                "SoH_Old, SoH_New, Fee, Payment_ID, Status, Swap_Time, Booking_ID " +
                "FROM dbo.SwapTransaction " +
                "WHERE Driver_ID = ? AND ChargingStation_ID = ? " +
                "ORDER BY Swap_Time DESC";
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ps.setInt(2, chargingStationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ✅ Thêm giao dịch swap mới (đã có ChargingStation_ID & xử lý NULL) và trả về ID
    public int insertSwapTransaction(SwapTransaction tx) {
        String sql = "INSERT INTO dbo.SwapTransaction " +
                "(Driver_ID, Staff_ID, Station_ID, ChargingStation_ID, Old_Battery, New_Battery, " +
                " SoH_Old, SoH_New, Fee, Payment_ID, Status, Swap_Time, Booking_ID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1 Driver_ID
            ps.setInt(1, tx.getDriver_ID());

            // 2 Staff_ID
            if (tx.getStaff_ID() <= 0) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, tx.getStaff_ID());

            // 3 Station_ID
            ps.setInt(3, tx.getStation_ID());

            // 4 ChargingStation_ID
            if (tx.getChargingStation_ID() <= 0) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, tx.getChargingStation_ID());

            // 5 Old_Battery
            if (tx.getOld_Battery() <= 0) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, tx.getOld_Battery());

            // 6 New_Battery (bắt buộc)
            ps.setInt(6, tx.getNew_Battery());

            // 7 SoH_Old
            if (tx.getSoH_Old() <= 0) ps.setNull(7, Types.DOUBLE);
            else ps.setDouble(7, tx.getSoH_Old());

            // 8 SoH_New
            if (tx.getSoH_New() <= 0) ps.setNull(8, Types.DOUBLE);
            else ps.setDouble(8, tx.getSoH_New());

            // 9 Fee
            if (tx.getFee() < 0) ps.setNull(9, Types.DECIMAL);
            else ps.setDouble(9, tx.getFee());

            // 10 Payment_ID
            if (tx.getPayment_ID() <= 0) ps.setNull(10, Types.INTEGER);
            else ps.setInt(10, tx.getPayment_ID());

            // 11 Status
            if (tx.getStatus() == null || tx.getStatus().isEmpty()) ps.setNull(11, Types.NVARCHAR);
            else ps.setString(11, tx.getStatus());

            // 12 Swap_Time
            Timestamp ts = tx.getSwap_Time();
            if (ts == null) ps.setNull(12, Types.TIMESTAMP);
            else ps.setTimestamp(12, ts);

            // 13 Booking_ID
            if (tx.getBooking_ID() <= 0) ps.setNull(13, Types.INTEGER);
            else ps.setInt(13, tx.getBooking_ID());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    // Lấy giao dịch swap theo ID
    public SwapTransaction getSwapTransactionById(int transactionId) {
        String sql = "SELECT ID, Driver_ID, Staff_ID, Station_ID, ChargingStation_ID, Old_Battery, New_Battery, " +
                "SoH_Old, SoH_New, Fee, Payment_ID, Status, Swap_Time, Booking_ID " +
                "FROM dbo.SwapTransaction WHERE ID = ?";
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // Cập nhật trạng thái giao dịch swap
    public boolean updateSwapTransactionStatus(int transactionId, String status) {
        String sql = "UPDATE dbo.SwapTransaction SET Status = ? WHERE ID = ?";
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, transactionId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Cập nhật Payment_ID cho giao dịch swap
    public boolean updateSwapTransactionPaymentId(int transactionId, int paymentId) {
        String sql = "UPDATE dbo.SwapTransaction SET Payment_ID = ? WHERE ID = ?";
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (paymentId <= 0) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, paymentId);
            ps.setInt(2, transactionId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Cập nhật Booking_ID cho giao dịch swap
    public boolean updateSwapTransactionBookingId(int transactionId, int bookingId) {
        String sql = "UPDATE dbo.SwapTransaction SET Booking_ID = ? WHERE ID = ?";
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (bookingId <= 0) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, bookingId);
            ps.setInt(2, transactionId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Danh sách swap theo Booking_ID
    public List<SwapTransaction> getSwapsByBookingId(int bookingId) {
        String sql = "SELECT ID, Driver_ID, Staff_ID, Station_ID, ChargingStation_ID, Old_Battery, New_Battery, " +
                "SoH_Old, SoH_New, Fee, Payment_ID, Status, Swap_Time, Booking_ID " +
                "FROM dbo.SwapTransaction WHERE Booking_ID = ? ORDER BY Swap_Time DESC";
        List<SwapTransaction> list = new ArrayList<>();
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ✅ Danh sách swap theo Driver_ID (giới hạn)
    public List<SwapTransaction> getSwapsByDriverId(int driverId, int limit) {
        String sql = "SELECT " +
                "ID, Driver_ID, Staff_ID, Station_ID, ChargingStation_ID, Old_Battery, New_Battery, " +
                "SoH_Old, SoH_New, Fee, Payment_ID, Status, Swap_Time, Booking_ID " +
                "FROM dbo.SwapTransaction WHERE Driver_ID = ? " +
                "ORDER BY Swap_Time DESC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        List<SwapTransaction> list = new ArrayList<>();
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ===== Helper: map 1 row ResultSet -> DTO (đủ cột, có ChargingStation_ID) =====
    private SwapTransaction mapRow(ResultSet rs) throws Exception {
        SwapTransaction tx = new SwapTransaction();
        tx.setID(rs.getInt("ID"));
        tx.setDriver_ID(rs.getInt("Driver_ID"));

        int staff = rs.getInt("Staff_ID");
        if (rs.wasNull()) staff = 0;
        tx.setStaff_ID(staff);

        tx.setStation_ID(rs.getInt("Station_ID"));

        int cs = rs.getInt("ChargingStation_ID");
        if (rs.wasNull()) cs = 0;
        tx.setChargingStation_ID(cs);

        int oldBat = rs.getInt("Old_Battery");
        if (rs.wasNull()) oldBat = 0;
        tx.setOld_Battery(oldBat);

        tx.setNew_Battery(rs.getInt("New_Battery"));

        double sohOld = rs.getDouble("SoH_Old");
        if (rs.wasNull()) sohOld = 0;
        tx.setSoH_Old(sohOld);

        double sohNew = rs.getDouble("SoH_New");
        if (rs.wasNull()) sohNew = 0;
        tx.setSoH_New(sohNew);

        double fee = rs.getDouble("Fee");
        if (rs.wasNull()) fee = -1;
        tx.setFee(fee);

        int payId = rs.getInt("Payment_ID");
        if (rs.wasNull()) payId = 0;
        tx.setPayment_ID(payId);

        tx.setStatus(rs.getString("Status"));
        tx.setSwap_Time(rs.getTimestamp("Swap_Time"));

        int bookingId = rs.getInt("Booking_ID");
        if (rs.wasNull()) bookingId = 0;
        tx.setBooking_ID(bookingId);

        return tx;
    }

    // ===== Thống kê giờ cao điểm =====
    /**
     * Thống kê số lượt đổi pin và doanh thu theo các khung giờ trong ngày
     * Chia 24 giờ thành 24 khung giờ (00:00-01:00, 01:00-02:00, ..., 23:00-00:00)
     * @param startDate Ngày bắt đầu (có thể null để lấy tất cả)
     * @param endDate Ngày kết thúc (có thể null để lấy tất cả)
     * @return Danh sách thống kê theo từng khung giờ
     */
    public List<PeakHourStatistics> getPeakHourStatistics(Date startDate, Date endDate) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    DATEPART(HOUR, Swap_Time) AS HourOfDay, ");
        sql.append("    COUNT(*) AS SwapCount, ");
        sql.append("    ISNULL(SUM(Fee), 0) AS TotalRevenue, ");
        sql.append("    ISNULL(AVG(Fee), 0) AS AverageFee ");
        sql.append("FROM dbo.SwapTransaction ");
        sql.append("WHERE Status = 'Completed' ");
        
        if (startDate != null && endDate != null) {
            sql.append("AND Swap_Time >= ? AND Swap_Time < DATEADD(DAY, 1, ?) ");
        } else if (startDate != null) {
            sql.append("AND Swap_Time >= ? ");
        } else if (endDate != null) {
            sql.append("AND Swap_Time < DATEADD(DAY, 1, ?) ");
        }
        
        sql.append("GROUP BY DATEPART(HOUR, Swap_Time) ");
        sql.append("ORDER BY HourOfDay");

        List<PeakHourStatistics> stats = new ArrayList<>();
        
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (startDate != null && endDate != null) {
                ps.setDate(paramIndex++, startDate);
                ps.setDate(paramIndex++, endDate);
            } else if (startDate != null) {
                ps.setDate(paramIndex++, startDate);
            } else if (endDate != null) {
                ps.setDate(paramIndex++, endDate);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int hour = rs.getInt("HourOfDay");
                    int swapCount = rs.getInt("SwapCount");
                    double totalRevenue = rs.getDouble("TotalRevenue");
                    double averageFee = rs.getDouble("AverageFee");
                    
                    String timeSlot = String.format("%02d:00-%02d:00", hour, (hour + 1) % 24);
                    
                    PeakHourStatistics stat = new PeakHourStatistics(
                        timeSlot, 
                        swapCount, 
                        totalRevenue, 
                        averageFee
                    );
                    stats.add(stat);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return stats;
    }

    /**
     * Thống kê giờ cao điểm theo Station_ID
     * @param stationId ID của trạm
     * @param startDate Ngày bắt đầu (có thể null)
     * @param endDate Ngày kết thúc (có thể null)
     * @return Danh sách thống kê theo từng khung giờ cho trạm cụ thể
     */
    public List<PeakHourStatistics> getPeakHourStatisticsByStation(int stationId, Date startDate, Date endDate) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    DATEPART(HOUR, Swap_Time) AS HourOfDay, ");
        sql.append("    COUNT(*) AS SwapCount, ");
        sql.append("    ISNULL(SUM(Fee), 0) AS TotalRevenue, ");
        sql.append("    ISNULL(AVG(Fee), 0) AS AverageFee ");
        sql.append("FROM dbo.SwapTransaction ");
        sql.append("WHERE Status = 'Completed' AND Station_ID = ? ");
        
        if (startDate != null && endDate != null) {
            sql.append("AND Swap_Time >= ? AND Swap_Time < DATEADD(DAY, 1, ?) ");
        } else if (startDate != null) {
            sql.append("AND Swap_Time >= ? ");
        } else if (endDate != null) {
            sql.append("AND Swap_Time < DATEADD(DAY, 1, ?) ");
        }
        
        sql.append("GROUP BY DATEPART(HOUR, Swap_Time) ");
        sql.append("ORDER BY HourOfDay");

        List<PeakHourStatistics> stats = new ArrayList<>();
        
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            ps.setInt(paramIndex++, stationId);
            
            if (startDate != null && endDate != null) {
                ps.setDate(paramIndex++, startDate);
                ps.setDate(paramIndex++, endDate);
            } else if (startDate != null) {
                ps.setDate(paramIndex++, startDate);
            } else if (endDate != null) {
                ps.setDate(paramIndex++, endDate);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int hour = rs.getInt("HourOfDay");
                    int swapCount = rs.getInt("SwapCount");
                    double totalRevenue = rs.getDouble("TotalRevenue");
                    double averageFee = rs.getDouble("AverageFee");
                    
                    String timeSlot = String.format("%02d:00-%02d:00", hour, (hour + 1) % 24);
                    
                    PeakHourStatistics stat = new PeakHourStatistics(
                        timeSlot, 
                        swapCount, 
                        totalRevenue, 
                        averageFee
                    );
                    stats.add(stat);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return stats;
    }

    /**
     * Lấy top N khung giờ có nhiều giao dịch nhất
     * @param topN Số lượng khung giờ muốn lấy
     * @param startDate Ngày bắt đầu (có thể null)
     * @param endDate Ngày kết thúc (có thể null)
     * @return Danh sách top N khung giờ có nhiều giao dịch nhất
     */
    public List<PeakHourStatistics> getTopPeakHours(int topN, Date startDate, Date endDate) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TOP (?) ");
        sql.append("    DATEPART(HOUR, Swap_Time) AS HourOfDay, ");
        sql.append("    COUNT(*) AS SwapCount, ");
        sql.append("    ISNULL(SUM(Fee), 0) AS TotalRevenue, ");
        sql.append("    ISNULL(AVG(Fee), 0) AS AverageFee ");
        sql.append("FROM dbo.SwapTransaction ");
        sql.append("WHERE Status = 'Completed' ");
        
        if (startDate != null && endDate != null) {
            sql.append("AND Swap_Time >= ? AND Swap_Time < DATEADD(DAY, 1, ?) ");
        } else if (startDate != null) {
            sql.append("AND Swap_Time >= ? ");
        } else if (endDate != null) {
            sql.append("AND Swap_Time < DATEADD(DAY, 1, ?) ");
        }
        
        sql.append("GROUP BY DATEPART(HOUR, Swap_Time) ");
        sql.append("ORDER BY SwapCount DESC");

        List<PeakHourStatistics> stats = new ArrayList<>();
        
        try (Connection con = DBUtils.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            ps.setInt(paramIndex++, topN);
            
            if (startDate != null && endDate != null) {
                ps.setDate(paramIndex++, startDate);
                ps.setDate(paramIndex++, endDate);
            } else if (startDate != null) {
                ps.setDate(paramIndex++, startDate);
            } else if (endDate != null) {
                ps.setDate(paramIndex++, endDate);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int hour = rs.getInt("HourOfDay");
                    int swapCount = rs.getInt("SwapCount");
                    double totalRevenue = rs.getDouble("TotalRevenue");
                    double averageFee = rs.getDouble("AverageFee");
                    
                    String timeSlot = String.format("%02d:00-%02d:00", hour, (hour + 1) % 24);
                    
                    PeakHourStatistics stat = new PeakHourStatistics(
                        timeSlot, 
                        swapCount, 
                        totalRevenue, 
                        averageFee
                    );
                    stats.add(stat);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return stats;
    }
}
