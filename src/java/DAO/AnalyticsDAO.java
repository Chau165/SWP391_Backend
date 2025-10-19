package DAO;

import com.google.gson.JsonObject;
import mylib.DBUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsDAO {

    // SQL base cho tất cả trạm
    private static final String SQL_ALL
            = "SELECT \n"
            + "    s.Station_ID, \n"
            + "    s.Name AS Station_Name,\n"
            + "    -- Li-ion\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 1 AND b.SoH > 85 THEN 1 ELSE 0 END),0) AS LiIon_Good,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 1 AND b.SoH > 80 AND b.SoH <= 85 THEN 1 ELSE 0 END),0) AS LiIon_Average,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 1 AND b.SoH >= 75 AND b.SoH <= 80 THEN 1 ELSE 0 END),0) AS LiIon_Weak,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 1 AND b.SoH < 75 THEN 1 ELSE 0 END),0) AS LiIon_Below75,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 1 THEN 1 ELSE 0 END),0) AS LiIon_Total,\n"
            + "    -- LFP\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 2 AND b.SoH > 85 THEN 1 ELSE 0 END),0) AS LFP_Good,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 2 AND b.SoH > 80 AND b.SoH <= 85 THEN 1 ELSE 0 END),0) AS LFP_Average,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 2 AND b.SoH >= 75 AND b.SoH <= 80 THEN 1 ELSE 0 END),0) AS LFP_Weak,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 2 AND b.SoH < 75 THEN 1 ELSE 0 END),0) AS LFP_Below75,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 2 THEN 1 ELSE 0 END),0) AS LFP_Total\n"
            + "FROM BatterySwapDBVer2.dbo.Station s\n"
            + "LEFT JOIN BatterySwapDBVer2.dbo.Charging_Station cs ON cs.Station_ID = s.Station_ID\n"
            + "LEFT JOIN BatterySwapDBVer2.dbo.BatterySlot bs ON bs.ChargingStation_ID = cs.ChargingStation_ID\n"
            + "LEFT JOIN BatterySwapDBVer2.dbo.Battery b ON b.Battery_ID = bs.Battery_ID\n"
            + "GROUP BY s.Station_ID, s.Name\n"
            + "ORDER BY s.Station_ID;";

    // SQL có WHERE cho một trạm
    private static final String SQL_BY_ID
            = "SELECT \n"
            + "    s.Station_ID, \n"
            + "    s.Name AS Station_Name,\n"
            + "    -- Li-ion\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 1 AND b.SoH > 85 THEN 1 ELSE 0 END),0) AS LiIon_Good,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 1 AND b.SoH > 80 AND b.SoH <= 85 THEN 1 ELSE 0 END),0) AS LiIon_Average,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 1 AND b.SoH >= 75 AND b.SoH <= 80 THEN 1 ELSE 0 END),0) AS LiIon_Weak,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 1 AND b.SoH < 75 THEN 1 ELSE 0 END),0) AS LiIon_Below75,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 1 THEN 1 ELSE 0 END),0) AS LiIon_Total,\n"
            + "    -- LFP\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 2 AND b.SoH > 85 THEN 1 ELSE 0 END),0) AS LFP_Good,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 2 AND b.SoH > 80 AND b.SoH <= 85 THEN 1 ELSE 0 END),0) AS LFP_Average,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 2 AND b.SoH >= 75 AND b.SoH <= 80 THEN 1 ELSE 0 END),0) AS LFP_Weak,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 2 AND b.SoH < 75 THEN 1 ELSE 0 END),0) AS LFP_Below75,\n"
            + "    COALESCE(SUM(CASE WHEN b.Type_ID = 2 THEN 1 ELSE 0 END),0) AS LFP_Total\n"
            + "FROM BatterySwapDBVer2.dbo.Station s\n"
            + "LEFT JOIN BatterySwapDBVer2.dbo.Charging_Station cs ON cs.Station_ID = s.Station_ID\n"
            + "LEFT JOIN BatterySwapDBVer2.dbo.BatterySlot bs ON bs.ChargingStation_ID = cs.ChargingStation_ID\n"
            + "LEFT JOIN BatterySwapDBVer2.dbo.Battery b ON b.Battery_ID = bs.Battery_ID\n"
            + "WHERE s.Station_ID = ?\n"
            + "GROUP BY s.Station_ID, s.Name\n"
            + "ORDER BY s.Station_ID;";

    /**
     * GIỮ NGUYÊN: trả tất cả trạm
     * @return 
     * @throws java.lang.ClassNotFoundException
     */
    public List<JsonObject> getStationBatterySummariesJson() throws ClassNotFoundException {
        return getStationBatterySummariesJson(null);
    }

    /**
     * MỚI: nếu stationId != null -> lọc theo 1 trạm; null -> tất cả
     * @param stationId
     * @return 
     * @throws java.lang.ClassNotFoundException
     */
    public List<JsonObject> getStationBatterySummariesJson(Integer stationId) throws ClassNotFoundException {
        List<JsonObject> list = new ArrayList<>();
        String sql = (stationId == null) ? SQL_ALL : SQL_BY_ID;

        try ( Connection con = DBUtils.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {

            if (stationId != null) {
                ps.setInt(1, stationId);
            }

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // --- Li-ion ---
                    JsonObject liIonRow = new JsonObject();
                    liIonRow.addProperty("stationId", rs.getInt("Station_ID"));
                    liIonRow.addProperty("stationName", rs.getString("Station_Name"));
                    liIonRow.addProperty("batteryType", "Li-ion");
                    liIonRow.addProperty("Good", rs.getInt("LiIon_Good"));
                    liIonRow.addProperty("Average", rs.getInt("LiIon_Average"));
                    liIonRow.addProperty("Weak", rs.getInt("LiIon_Weak"));
                    liIonRow.addProperty("Below75", rs.getInt("LiIon_Below75")); // dùng trực tiếp từ SQL
                    liIonRow.addProperty("Total", rs.getInt("LiIon_Total"));
                    list.add(liIonRow);

                    // --- LFP ---
                    JsonObject lfpRow = new JsonObject();
                    lfpRow.addProperty("stationId", rs.getInt("Station_ID"));
                    lfpRow.addProperty("stationName", rs.getString("Station_Name"));
                    lfpRow.addProperty("batteryType", "LFP");
                    lfpRow.addProperty("Good", rs.getInt("LFP_Good"));
                    lfpRow.addProperty("Average", rs.getInt("LFP_Average"));
                    lfpRow.addProperty("Weak", rs.getInt("LFP_Weak"));
                    lfpRow.addProperty("Below75", rs.getInt("LFP_Below75")); // dùng trực tiếp từ SQL
                    lfpRow.addProperty("Total", rs.getInt("LFP_Total"));
                    list.add(lfpRow);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * ========== THÁNG HIỆN TẠI (AUTO-ROLL) ==========
     */
    /**
     * All stations: thống kê Đổi pin trong THÁNG HIỆN TẠI
     * @return 
     * @throws java.lang.ClassNotFoundException
     */
    public List<JsonObject> getCurrentMonthSwapStatsAllStations() throws ClassNotFoundException {
        List<JsonObject> list = new ArrayList<>();

        // mốc thời gian tháng hiện tại [start, nextMonthStart)
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate startDate = now.withDayOfMonth(1);
        java.time.LocalDate nextMonthStart = startDate.plusMonths(1);

        String sql
                = "SELECT s.Station_ID, s.Name AS Station_Name, "
                + "       COALESCE(COUNT(pt.ID), 0)   AS SwapCount, "
                + "       COALESCE(SUM(pt.Amount), 0) AS SwapRevenue "
                + "FROM BatterySwapDBVer2.dbo.Station s "
                + "LEFT JOIN BatterySwapDBVer2.dbo.PaymentTransaction pt "
                + "  ON pt.Station_ID = s.Station_ID "
                + " AND pt.Transaction_Time >= ? "
                + " AND pt.Transaction_Time < ? "
                + " AND pt.Description LIKE 'Swap Battery%' "
                + "GROUP BY s.Station_ID, s.Name "
                + "ORDER BY s.Station_ID;";

        try ( Connection con = DBUtils.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, java.sql.Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(nextMonthStart.atStartOfDay()));

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("stationId", rs.getInt("Station_ID"));
                    obj.addProperty("stationName", rs.getString("Station_Name"));
                    obj.addProperty("year", startDate.getYear());
                    obj.addProperty("month", startDate.getMonthValue());
                    obj.addProperty("swapCount", rs.getInt("SwapCount"));
                    obj.addProperty("swapRevenue",
                            rs.getBigDecimal("SwapRevenue") == null ? 0
                            : rs.getBigDecimal("SwapRevenue").doubleValue());
                    list.add(obj);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * One station: thống kê Đổi pin trong THÁNG HIỆN TẠI cho 1 trạm
     * @param stationId
     * @return 
     * @throws java.lang.ClassNotFoundException
     */
    public JsonObject getCurrentMonthSwapStatsByStation(int stationId) throws ClassNotFoundException {
        JsonObject obj = new JsonObject();

        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate startDate = now.withDayOfMonth(1);
        java.time.LocalDate nextMonthStart = startDate.plusMonths(1);

        String sql
                = "SELECT s.Station_ID, s.Name AS Station_Name, "
                + "       COALESCE(COUNT(pt.ID), 0)   AS SwapCount, "
                + "       COALESCE(SUM(pt.Amount), 0) AS SwapRevenue "
                + "FROM BatterySwapDBVer2.dbo.Station s "
                + "LEFT JOIN BatterySwapDBVer2.dbo.PaymentTransaction pt "
                + "  ON pt.Station_ID = s.Station_ID "
                + " AND pt.Transaction_Time >= ? "
                + " AND pt.Transaction_Time < ? "
                + " AND pt.Description LIKE 'Swap Battery%' "
                + "WHERE s.Station_ID = ? "
                + "GROUP BY s.Station_ID, s.Name;";

        try ( Connection con = DBUtils.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, java.sql.Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(nextMonthStart.atStartOfDay()));
            ps.setInt(3, stationId);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    obj.addProperty("stationId", rs.getInt("Station_ID"));
                    obj.addProperty("stationName", rs.getString("Station_Name"));
                    obj.addProperty("year", startDate.getYear());
                    obj.addProperty("month", startDate.getMonthValue());
                    obj.addProperty("swapCount", rs.getInt("SwapCount"));
                    obj.addProperty("swapRevenue",
                            rs.getBigDecimal("SwapRevenue") == null ? 0
                            : rs.getBigDecimal("SwapRevenue").doubleValue());
                } else {
                    // nếu trạm không tồn tại, trả rỗng theo convention của bạn
                    obj.addProperty("stationId", stationId);
                    obj.addProperty("stationName", "");
                    obj.addProperty("year", startDate.getYear());
                    obj.addProperty("month", startDate.getMonthValue());
                    obj.addProperty("swapCount", 0);
                    obj.addProperty("swapRevenue", 0.0);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
