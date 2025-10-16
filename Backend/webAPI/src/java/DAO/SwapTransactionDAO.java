package DAO;

import DTO.Station;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mylib.DBUtils;

public class SwapTransactionDAO {

    // Return true if user has at least one swap_transaction with Status='Completed'.
    // Only Driver role is allowed to comment; check Driver_ID and Status='Completed'.
    public static boolean userHasSwapTransactions(int userId, String role) {
        // We only consider Driver_ID for allowing comments. Staff comments are disabled.
        // IMPORTANT: Only completed swaps allow commenting.
        // Try multiple matching strategies to handle various database configurations
        
        // Strategy 1: Exact case-insensitive match with COLLATE (most reliable)
    String sql1 = "SELECT TOP 1 ID FROM SwapTransaction " +
             "WHERE Driver_ID = ? AND Status COLLATE SQL_Latin1_General_CP1_CI_AS = 'Completed'";
        
        // Strategy 2: UPPER + TRIM (current)
    String sql2 = "SELECT TOP 1 ID FROM SwapTransaction " +
                     "WHERE Driver_ID = ? AND UPPER(LTRIM(RTRIM(Status))) = 'COMPLETED'";
        
        // Strategy 3: LIKE pattern (fallback)
    String sql3 = "SELECT TOP 1 ID FROM SwapTransaction " +
                     "WHERE Driver_ID = ? AND Status LIKE '%ompleted%'";

        System.out.println("DEBUG userHasSwapTransactions - userId: " + userId + ", role: " + role);
        
        // Try strategy 1 first
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql1)) {
            ps.setInt(1, userId);
            System.out.println("DEBUG - Trying Strategy 1 (COLLATE)");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("DEBUG - Strategy 1 SUCCESS: Found swap ID=" + rs.getInt(1));
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("DEBUG - Strategy 1 FAILED: " + e.getMessage());
        }
        
        // Try strategy 2
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql2)) {
            ps.setInt(1, userId);
            System.out.println("DEBUG - Trying Strategy 2 (UPPER+TRIM)");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("DEBUG - Strategy 2 SUCCESS: Found swap ID=" + rs.getInt(1));
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("DEBUG - Strategy 2 FAILED: " + e.getMessage());
        }
        
        // Try strategy 3 as last resort
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql3)) {
            ps.setInt(1, userId);
            System.out.println("DEBUG - Trying Strategy 3 (LIKE)");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("DEBUG - Strategy 3 SUCCESS: Found swap ID=" + rs.getInt(1));
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("DEBUG - Strategy 3 FAILED: " + e.getMessage());
        }
        
        // Also try to log actual Status values for debugging
        try (Connection conn = DBUtils.getConnection()) {
            String debugSql = "SELECT TOP 5 ID, Driver_ID, Status FROM SwapTransaction WHERE Driver_ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(debugSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("DEBUG - Actual Status values in database for Driver_ID=" + userId + ":");
                    while (rs.next()) {
                        String status = rs.getString("Status");
                        System.out.println("  Swap ID=" + rs.getInt("ID") + ", Status=[" + status + "], Length=" + (status != null ? status.length() : 0));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("DEBUG - Failed to log actual Status values: " + e.getMessage());
        }
        
        System.out.println("DEBUG userHasSwapTransactions - ALL STRATEGIES FAILED - Result: false");
        return false;
    }

    // Return list of stations used by user with the swap transaction id associated (one per swap id)
    // Only returns stations where the user was the Driver AND Status='Completed'.
    public static List<StationWithSwap> getStationsWithSwapIdsByUser(int userId, String role) {
        List<StationWithSwap> list = new ArrayList<>();
        // Only return stations where the user was the Driver in swap_transactions with Completed status.
        // Use LTRIM(RTRIM()) to handle whitespace and case-insensitive comparison
    String sqlDriver = "SELECT DISTINCT s.ID AS SwapID, st.Station_ID, st.Name, st.Address "
        + "FROM SwapTransaction s JOIN Station st ON s.Station_ID = st.Station_ID "
                + "WHERE s.Driver_ID = ? AND UPPER(LTRIM(RTRIM(s.Status))) = 'COMPLETED'";
        String sql = sqlDriver;

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            System.out.println("DEBUG getStationsWithSwapIdsByUser - userId: " + userId + ", role: " + role);
            System.out.println("DEBUG - SQL Query: " + sql);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StationWithSwap info = new StationWithSwap();
                    info.swapId = rs.getInt("SwapID");
                    info.stationId = rs.getInt("Station_ID");
                    info.name = rs.getString("Name");
                    info.address = rs.getString("Address");
                    info.totalBattery = 0; // TODO: Fix when know correct column name
                    list.add(info);
                    System.out.println("DEBUG - Found swap: SwapID=" + info.swapId + ", Station=" + info.name + " (StationID=" + info.stationId + ")");
                }
                System.out.println("DEBUG - Total stations found: " + list.size());
            }
        } catch (Exception e) {
            System.err.println("ERROR in getStationsWithSwapIdsByUser:");
            e.printStackTrace();
        }
        return list;
    }

    public static class StationWithSwap {
        public int swapId;
        public int stationId;
        public String name;
        public String address;
        public int totalBattery;
    }
}
