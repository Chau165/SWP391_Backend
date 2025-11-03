package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mylib.DBUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "StationUpgradeSuggestionController", urlPatterns = {"/admin/api/upgrade_suggestions"})
public class StationUpgradeSuggestionController extends HttpServlet {

    private final Gson gson = new Gson();

    // Thresholds - tweak as needed
    private static final double GROWTH_THRESHOLD_PERCENT = 30.0; // percent growth week-over-week
    private static final int OVERLOAD_DAYS_THRESHOLD = 2; // days in last 7 with overload
    private static final double FAIL_RATE_THRESHOLD = 0.2; // 20% failure/uncompleted
    private static final double SOH_LOW_THRESHOLD = 50.0; // average SoH below 50%

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        List<Map<String, Object>> results = new ArrayList<>();

        // Fetch station info with actual names and slot counts
        String csSql = "SELECT s.Station_ID, " +
                       "ISNULL(s.Name, 'Station #' + CAST(s.Station_ID AS VARCHAR)) as StationName, " +
                       "ISNULL(s.Address, '') as Address, " +
                       "COUNT(DISTINCT bs.Slot_ID) as SlotCount " +
                       "FROM Station s " +
                       "LEFT JOIN BatterySlot bs ON s.Station_ID = bs.ChargingStation_ID " +
                       "GROUP BY s.Station_ID, s.Name, s.Address";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(csSql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int stationId = rs.getInt("Station_ID");
                String stationName = rs.getString("StationName");
                String address = rs.getString("Address");
                int slotCap = rs.getInt("SlotCount");

                Map<String, Object> item = new HashMap<>();
                item.put("stationId", stationId);
                item.put("stationName", stationName);
                item.put("address", address);
                item.put("slotCapacity", slotCap);

                // compute metrics
                // 1) daily counts last 14 days - use Booking table (Expired status = completed swaps)
                String dailySql = "SELECT CAST(Expired_Date AS DATE) as d, COUNT(*) as cnt " +
                                  "FROM Booking " +
                                  "WHERE Station_ID = ? " +
                                  "AND Status = 'Expired' " +
                                  "AND Expired_Date >= DATEADD(day, -14, GETDATE()) " +
                                  "GROUP BY CAST(Expired_Date AS DATE) ORDER BY d";
                List<Map<String, Object>> dailyCountsArray = new ArrayList<>();
                List<Integer> dailyCounts = new ArrayList<>();
                int totalTransactions = 0;
                
                try (PreparedStatement ps2 = conn.prepareStatement(dailySql)) {
                    ps2.setInt(1, stationId);
                    try (ResultSet r2 = ps2.executeQuery()) {
                        while (r2.next()) {
                            int cnt = r2.getInt("cnt");
                            String day = r2.getString("d");
                            dailyCounts.add(cnt);
                            totalTransactions += cnt;
                            
                            Map<String, Object> dayData = new HashMap<>();
                            dayData.put("day", day);
                            dayData.put("count", cnt);
                            dailyCountsArray.add(dayData);
                        }
                        
                        // Return structured dailyCounts for chart
                        item.put("dailyCounts", dailyCountsArray);
                        item.put("sampleSize", totalTransactions);
                        item.put("totalTransactions", totalTransactions);

                        // ====================================================================
                        // METRIC CALCULATIONS (Chi tiết công thức tính):
                        // ====================================================================
                        // 1. last7Avg = Tổng swap trong 7 ngày gần nhất / 7
                        //    - Lấy 7 ngày cuối trong dailyCounts array (từ hôm nay về trước)
                        //    - Nếu có ít hơn 7 ngày dữ liệu, chia cho số ngày thực tế có
                        // 2. prev7Avg = Tổng swap trong 7 ngày trước đó (ngày 8-14) / 7
                        //    - Lấy 7 ngày trước last7 days
                        //    - Nếu không đủ 7 ngày, chia cho số ngày thực tế có
                        // 3. overloadDays = Số ngày mà swap_count > slot capacity trong 14 ngày
                        // 4. growthPercent = ((last7Avg - prev7Avg) / prev7Avg) * 100
                        // ====================================================================
                        
                        double last7Avg = 0.0, prev7Avg = 0.0;
                        int last7Days = 0, prev7Days = 0; // Track actual days used for calculation
                        
                        if (!dailyCounts.isEmpty()) {
                            int n = dailyCounts.size();
                            
                            // Calculate last 7 days average (most recent days)
                            int upto = Math.min(7, n);
                            int sumLast7 = 0;
                            for (int i = 0; i < upto; i++) {
                                sumLast7 += dailyCounts.get(n - 1 - i);
                            }
                            last7Days = upto;
                            last7Avg = upto > 0 ? (double) sumLast7 / upto : 0.0;

                            // Calculate previous 7 days average (days 8-14 before today)
                            int prevCount = Math.min(7, Math.max(0, n - upto));
                            int sumPrev = 0;
                            for (int i = 0; i < prevCount; i++) {
                                sumPrev += dailyCounts.get(n - 1 - upto - i);
                            }
                            prev7Days = prevCount;
                            prev7Avg = prevCount > 0 ? (double) sumPrev / prevCount : 0.0;
                        }
                        
                        item.put("last7Avg", last7Avg);
                        item.put("prev7Avg", prev7Avg);
                        item.put("last7Days", last7Days); // Actual days used in calculation
                        item.put("prev7Days", prev7Days); // Actual days used in calculation

                        double growthPercent = 0.0;
                        if (prev7Avg > 0.0) {
                            growthPercent = ((last7Avg - prev7Avg) / prev7Avg) * 100.0;
                        }
                        item.put("growthPercent", growthPercent);

                        // Count overload days: days where swap_count > slot capacity
                        int overloadDays = 0;
                        if (slotCap > 0) { // Only count if slot capacity is known
                            for (int v : dailyCounts) {
                                if (v > slotCap) overloadDays++;
                            }
                        }
                        item.put("overloadDays", overloadDays);
                    }
                }

                // 2) failure/uncompleted rate last 7 days - use Booking Status
                // Consider 'Expired' = success, others (Pending, Cancelled, etc) = failed/incomplete
                String failSql = "SELECT COUNT(*) AS total, " +
                                 "SUM(CASE WHEN Status <> 'Expired' THEN 1 ELSE 0 END) AS failed " +
                                 "FROM Booking " +
                                 "WHERE Station_ID = ? " +
                                 "AND Booking_Time >= DATEADD(day, -7, GETDATE())";
                try (PreparedStatement ps3 = conn.prepareStatement(failSql)) {
                    ps3.setInt(1, stationId);
                    try (ResultSet r3 = ps3.executeQuery()) {
                        if (r3.next()) {
                            int total = r3.getInt("total");
                            int failed = r3.getInt("failed");
                            double failRate = total > 0 ? (double) failed / total : 0.0;
                            item.put("totalLast7", total);
                            item.put("failedLast7", failed);
                            item.put("failRate", String.format("%.0f%%", failRate * 100));
                            item.put("failRateNumeric", failRate); // keep numeric for thresholds
                        }
                    }
                }

                // 3) Avg Battery Condition in last 30 days (use BatterySlot Condition if available)
                // For now, skip SoH calculation since SwapTransaction doesn't exist
                // We'll use slot condition stats instead
                String sohSql = "SELECT " +
                                "COUNT(*) as totalSlots, " +
                                "SUM(CASE WHEN Condition = 'Good' THEN 1 ELSE 0 END) as goodCount, " +
                                "SUM(CASE WHEN Condition = 'Damage' THEN 1 ELSE 0 END) as damageCount " +
                                "FROM BatterySlot WHERE ChargingStation_ID = ?";
                try (PreparedStatement ps4 = conn.prepareStatement(sohSql)) {
                    ps4.setInt(1, stationId);
                    try (ResultSet r4 = ps4.executeQuery()) {
                        if (r4.next()) {
                            int totalSlots = r4.getInt("totalSlots");
                            int goodCount = r4.getInt("goodCount");
                            int damageCount = r4.getInt("damageCount");
                            // Calculate a pseudo-SoH based on good slots ratio
                            double avgSoH = totalSlots > 0 ? ((double)goodCount / totalSlots) * 100.0 : 100.0;
                            item.put("avgSoHOld", avgSoH);
                            item.put("totalSlots", totalSlots);
                            item.put("goodSlots", goodCount);
                            item.put("damagedSlots", damageCount);
                        }
                    }
                }

                // ====================================================================
                // RECOMMENDATION ENGINE - Build AI-driven upgrade suggestions
                // ====================================================================
                List<String> recs = new ArrayList<>();
                StringBuilder evidence = new StringBuilder();

                double growth = item.get("growthPercent") instanceof Number ? ((Number) item.get("growthPercent")).doubleValue() : 0.0;
                int overload = item.get("overloadDays") instanceof Number ? ((Number) item.get("overloadDays")).intValue() : 0;
                double failRate = item.get("failRateNumeric") instanceof Number ? ((Number) item.get("failRateNumeric")).doubleValue() : 0.0;
                double avgSoH = item.get("avgSoHOld") instanceof Number ? ((Number) item.get("avgSoHOld")).doubleValue() : 100.0;
                double last7Avg = item.get("last7Avg") instanceof Number ? ((Number) item.get("last7Avg")).doubleValue() : 0.0;
                int sampleSize = item.get("sampleSize") instanceof Number ? ((Number) item.get("sampleSize")).intValue() : 0;

                // Calculate capacity utilization (critical metric)
                double capacityUtilization = slotCap > 0 ? (last7Avg / slotCap) * 100.0 : -1.0; // -1 = unknown
                item.put("capacityUtilization", capacityUtilization);

                // ====================================================================
                // RULE 1: Insufficient data (< 5 swaps in 14 days)
                // STATUS = WARNING_DATA (never Critical when data insufficient)
                // ====================================================================
                if (sampleSize < 5) {
                    item.put("status", "WARNING_DATA"); // Special status for insufficient data
                    item.put("recommendation", "⚠️ Không đủ dữ liệu để dự báo chính xác");
                    item.put("evidence", "Chỉ có " + sampleSize + " giao dịch trong 14 ngày - cần ít nhất 5 giao dịch để phân tích đáng tin cậy");
                    item.put("dataInsufficient", true); // Flag for frontend
                    results.add(item);
                    continue; // Skip to next station
                }
                
                // ====================================================================
                // EDGE CASE: Slot capacity unknown (slotCap = 0 or N/A)
                // ====================================================================
                if (slotCap <= 0) {
                    item.put("status", "WARNING_DATA");
                    item.put("recommendation", "⚠️ Thiếu dữ liệu slot capacity - Kiểm tra lại cấu hình trạm!");
                    item.put("evidence", "Không xác định được số slot tại trạm này - cần cập nhật BatterySlot table");
                    item.put("slotCapacityMissing", true); // Flag for frontend
                    results.add(item);
                    continue; // Skip to next station
                }

                // ====================================================================
                // RULE 2: CRITICAL - Any of these conditions trigger immediate action
                // ====================================================================
                boolean isCritical = false;
                
                if (failRate >= 0.20) { // 20% or more failures
                    recs.add("🚨 KHẨN CẤP: Tỷ lệ lỗi rất cao (" + String.format("%.0f", failRate * 100) + "%) - Kiểm tra thiết bị ngay!");
                    evidence.append("Tỷ lệ lỗi: ").append(String.format("%.0f", failRate * 100)).append("%. ");
                    isCritical = true;
                }
                
                if (overload >= 2) { // 2+ days overloaded
                    recs.add("🚨 KHẨN CẤP: Quá tải " + overload + " ngày trong 14 ngày - Nâng cấp slot ngay!");
                    evidence.append("Quá tải: ").append(overload).append(" ngày. ");
                    isCritical = true;
                }
                
                if (last7Avg >= slotCap) { // Average demand exceeds capacity
                    recs.add("🚨 KHẨN CẤP: Nhu cầu trung bình (" + String.format("%.1f", last7Avg) + ") vượt số slot (" + slotCap + ") - Tăng capacity ngay!");
                    evidence.append("Nhu cầu > Capacity: ").append(String.format("%.1f", last7Avg)).append(" >= ").append(slotCap).append(". ");
                    isCritical = true;
                }
                
                if (isCritical) {
                    item.put("status", "CRITICAL");
                }
                
                // ====================================================================
                // RULE 3: WARNING - Any of these conditions need monitoring
                // ====================================================================
                boolean isWarning = false;
                
                if (!isCritical && failRate >= 0.10) { // 10-19% failures
                    recs.add("⚠️ CẢNH BÁO: Tỷ lệ lỗi cao (" + String.format("%.0f", failRate * 100) + "%) - Cần kiểm tra");
                    evidence.append("Tỷ lệ lỗi: ").append(String.format("%.0f", failRate * 100)).append("%. ");
                    isWarning = true;
                }
                
                if (!isCritical && overload == 1) { // Exactly 1 day overloaded
                    recs.add("⚠️ CẢNH BÁO: Có 1 ngày quá tải - Theo dõi xu hướng");
                    evidence.append("Quá tải: 1 ngày. ");
                    isWarning = true;
                }
                
                if (!isCritical && last7Avg >= slotCap * 0.8) { // 80-99% capacity
                    double capacityPercent = (last7Avg / slotCap) * 100.0;
                    recs.add("⚠️ CẢNH BÁO: Công suất cao (" + String.format("%.0f", capacityPercent) + "%) - Cần chuẩn bị nâng cấp");
                    evidence.append("Công suất: ").append(String.format("%.0f", capacityPercent)).append("%. ");
                    isWarning = true;
                }
                
                if (!isCritical && growth >= 50.0) { // Rapid growth 50%+
                    recs.add("⚠️ CẢNH BÁO: Tăng trưởng nhanh (" + String.format("%.0f", growth) + "%) - Lên kế hoạch mở rộng");
                    evidence.append("Tăng trưởng: ").append(String.format("%.1f", growth)).append("%. ");
                    isWarning = true;
                }
                
                if (isWarning) {
                    item.put("status", "WARNING");
                }
                
                // ====================================================================
                // RULE 4: OK - All metrics healthy
                // ====================================================================
                if (!isCritical && !isWarning) {
                    item.put("status", "OK");
                    recs.add("✅ Trạm hoạt động ổn định");
                    evidence.append("Tất cả chỉ số trong ngưỡng an toàn. ");
                }
                
                // ====================================================================
                // Additional recommendations (not affecting status)
                // ====================================================================
                if (avgSoH > 0 && avgSoH < SOH_LOW_THRESHOLD) {
                    recs.add("💡 Gợi ý: Bảo dưỡng/thay pin (SoH: " + String.format("%.0f", avgSoH) + "%)");
                    evidence.append("Pin: ").append(String.format("%.0f", avgSoH)).append("%. ");
                }

                // Set final recommendation text
                item.put("recommendation", String.join(" | ", recs));
                item.put("evidence", evidence.toString());

                results.add(item);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                // Avoid Java 9+ Map.of to keep compatibility with older Java (project uses Java 8)
                Map<String, String> err = new HashMap<>();
                err.put("error", ex.getMessage());
                out.print(gson.toJson(err));
                return;
            }
        }

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(results));
        }
    }
}
