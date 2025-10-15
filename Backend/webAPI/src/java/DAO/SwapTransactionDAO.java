package DAO;

import DTO.Station;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mylib.DBUtils;

public class SwapTransactionDAO {

    // Return true if user has at least one SwapTransaction with Status='Completed'.
    // Only Driver role is allowed to comment; check Driver_ID and Status='Completed'.
    public static boolean userHasSwapTransactions(int userId, String role) {
        // We only consider Driver_ID for allowing comments. Staff comments are disabled.
        // IMPORTANT: Only completed swaps allow commenting.
        // NOTE: Table name is SwapTransaction (not swap_transactions with underscore)
        String sql = "SELECT TOP 1 ID FROM SwapTransaction " +
                    "WHERE Driver_ID = ? AND Status = N'Completed'";

        System.out.println("DEBUG userHasSwapTransactions - userId: " + userId + ", role: " + role);
        
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            System.out.println("DEBUG - Executing query on SwapTransaction table");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int swapId = rs.getInt(1);
                    System.out.println("DEBUG - SUCCESS: Found completed swap ID=" + swapId);
                    return true;
                } else {
                    System.out.println("DEBUG - No completed swaps found for Driver_ID=" + userId);
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR in userHasSwapTransactions:");
            e.printStackTrace();
        }
        
        // Debug: Log actual Status values for this user
        try (Connection conn = DBUtils.getConnection()) {
            String debugSql = "SELECT TOP 5 ID, Driver_ID, Status FROM SwapTransaction WHERE Driver_ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(debugSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("DEBUG - Actual Status values in SwapTransaction for Driver_ID=" + userId + ":");
                    while (rs.next()) {
                        String status = rs.getString("Status");
                        System.out.println("  Swap ID=" + rs.getInt("ID") + ", Status=[" + status + "]");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("DEBUG - Failed to log actual Status values: " + e.getMessage());
        }
        
        return false;
    }

    // Return list of stations used by user with the swap transaction id associated (one per swap id)
    // Only returns stations where the user was the Driver AND Status='Completed'.
    public static List<StationWithSwap> getStationsWithSwapIdsByUser(int userId, String role) {
        List<StationWithSwap> list = new ArrayList<>();
        // NOTE: Table name is SwapTransaction (not swap_transactions with underscore)
        // NOTE: Station table doesn't have Total_Battery column based on schema
        String sql = "SELECT DISTINCT s.ID AS SwapID, st.Station_ID, st.Name, st.Address "
                + "FROM SwapTransaction s JOIN Station st ON s.Station_ID = st.Station_ID "
                + "WHERE s.Driver_ID = ? AND s.Status = N'Completed'";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            System.out.println("DEBUG getStationsWithSwapIdsByUser - userId: " + userId + ", role: " + role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StationWithSwap info = new StationWithSwap();
                    info.swapId = rs.getInt("SwapID");
                    info.stationId = rs.getInt("Station_ID");
                    info.name = rs.getString("Name");
                    info.address = rs.getString("Address");
                    info.totalBattery = 0; // Not available in schema
                    list.add(info);
                    System.out.println("DEBUG - Found station: " + info.name + " (ID: " + info.stationId + ", SwapID: " + info.swapId + ")");
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
