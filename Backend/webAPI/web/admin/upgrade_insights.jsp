<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.sql.*, java.util.*, mylib.DBUtils" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>AI Dự báo nâng cấp hạ tầng</title>
    <style>
        body{font-family: Arial, Helvetica, sans-serif; padding: 16px}
        table{border-collapse: collapse; width: 100%;}
        th, td{border: 1px solid #ddd; padding: 8px}
        th{background:#f4f4f4}
        .warn{background:#fff3cd}
        .ok{background:#e9f7ef}
        .muted{color:#666;margin-bottom:8px}
    </style>
</head>
<body>
    <h2>AI Dự báo nâng cấp hạ tầng</h2>
    <p class="muted">Danh sách khuyến nghị dựa trên dữ liệu lịch sử trong cơ sở dữ liệu — hiển thị trực tiếp, không cho phép xuất/xuất khẩu.</p>

    <%
        // Parameters / thresholds (match logic used in servlet version)
        final double GROWTH_THRESHOLD_PERCENT = 30.0;
        final int OVERLOAD_DAYS_THRESHOLD = 2;
        final double FAIL_RATE_THRESHOLD = 0.2;
        final double SOH_LOW_THRESHOLD = 50.0;

        class Suggestion { int csId; int stationId; String name; int slotCap; double last7Avg; double prev7Avg; double growth; int overloadDays; double failRate; double avgSoH; String recommendation; String evidence; }

        List<Suggestion> outList = new ArrayList<>();

        try (Connection conn = DBUtils.getConnection()) {
            String csSql = "SELECT ChargingStation_ID, Station_ID, Name, Slot_Capacity FROM Charging_Station";
            try (PreparedStatement ps = conn.prepareStatement(csSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Suggestion s = new Suggestion();
                    s.csId = rs.getInt("ChargingStation_ID");
                    s.stationId = rs.getInt("Station_ID");
                    s.name = rs.getString("Name");
                    s.slotCap = rs.getInt("Slot_Capacity");

                    // daily counts last 14 days
                    String dailySql = "SELECT CAST(Swap_Time AS DATE) as d, COUNT(*) as cnt FROM SwapTransaction WHERE ChargingStation_ID = ? AND Swap_Time >= DATEADD(day, -14, GETDATE()) GROUP BY CAST(Swap_Time AS DATE) ORDER BY d";
                    List<Integer> daily = new ArrayList<>();
                    try (PreparedStatement ps2 = conn.prepareStatement(dailySql)) {
                        ps2.setInt(1, s.csId);
                        try (ResultSet r2 = ps2.executeQuery()) {
                            while (r2.next()) daily.add(r2.getInt("cnt"));
                        }
                    }

                    double last7Avg = 0.0, prev7Avg = 0.0;
                    if (!daily.isEmpty()) {
                        int n = daily.size();
                        int upto = Math.min(7, n);
                        int sumLast7 = 0;
                        for (int i = 0; i < upto; i++) sumLast7 += daily.get(n - 1 - i);
                        last7Avg = upto > 0 ? (double) sumLast7 / upto : 0.0;
                        int prevCount = Math.min(7, Math.max(0, n - upto));
                        int sumPrev = 0;
                        for (int i = 0; i < prevCount; i++) sumPrev += daily.get(n - 1 - upto - i);
                        prev7Avg = prevCount > 0 ? (double) sumPrev / prevCount : 0.0;
                    }
                    s.last7Avg = last7Avg; s.prev7Avg = prev7Avg;
                    s.growth = (prev7Avg > 0) ? ((last7Avg - prev7Avg) / prev7Avg) * 100.0 : 0.0;

                    // overload days (count of days in daily[] where count > slotCap)
                    int overload = 0;
                    for (int v : daily) if (v > s.slotCap) overload++;
                    s.overloadDays = overload;

                    // fail rate last 7 days
                    String failSql = "SELECT COUNT(*) AS total, SUM(CASE WHEN UPPER(ISNULL(Status,'')) <> 'COMPLETED' THEN 1 ELSE 0 END) AS failed FROM SwapTransaction WHERE ChargingStation_ID = ? AND Swap_Time >= DATEADD(day, -7, GETDATE())";
                    try (PreparedStatement ps3 = conn.prepareStatement(failSql)) {
                        ps3.setInt(1, s.csId);
                        try (ResultSet r3 = ps3.executeQuery()) {
                            if (r3.next()) {
                                int total = r3.getInt("total");
                                int failed = r3.getInt("failed");
                                s.failRate = total > 0 ? (double) failed / total : 0.0;
                            }
                        }
                    }

                    // avg SoH old last 30 days
                    String sohSql = "SELECT AVG(SoH_Old) AS avgSoHOld FROM SwapTransaction WHERE ChargingStation_ID = ? AND SoH_Old IS NOT NULL AND Swap_Time >= DATEADD(day, -30, GETDATE())";
                    try (PreparedStatement ps4 = conn.prepareStatement(sohSql)) {
                        ps4.setInt(1, s.csId);
                        try (ResultSet r4 = ps4.executeQuery()) {
                            if (r4.next()) s.avgSoH = r4.getDouble("avgSoHOld");
                            else s.avgSoH = 100.0;
                        }
                    }

                    // build recommendations
                    StringBuilder recs = new StringBuilder();
                    StringBuilder evidence = new StringBuilder();
                    if (s.growth > GROWTH_THRESHOLD_PERCENT) {
                        if (recs.length()>0) recs.append("; ");
                        recs.append("Tăng số lượng slot/nhân sự (tăng trưởng " + (int)Math.round(s.growth) + "% )");
                        evidence.append("Tăng trưởng giao dịch: ").append(String.format("%.1f", s.growth)).append("%.");
                    }
                    if (s.overloadDays >= OVERLOAD_DAYS_THRESHOLD) {
                        if (recs.length()>0) recs.append("; ");
                        recs.append("Cân nhắc tăng slot hoặc phân ca ("+ s.overloadDays + " ngày quá tải)");
                        if (evidence.length()>0) evidence.append(" ");
                        evidence.append("Ngày quá tải: ").append(s.overloadDays).append(".");
                    }
                    if (s.failRate > FAIL_RATE_THRESHOLD) {
                        if (recs.length()>0) recs.append("; ");
                        recs.append("Kiểm tra thiết bị/nguồn (tỷ lệ giao dịch không hoàn thành " + (int)Math.round(s.failRate*100) + "% )");
                        if (evidence.length()>0) evidence.append(" ");
                        evidence.append("Tỷ lệ lỗi: ").append((int)Math.round(s.failRate*100)).append("%.");
                    }
                    if (s.avgSoH > 0 && s.avgSoH < SOH_LOW_THRESHOLD) {
                        if (recs.length()>0) recs.append("; ");
                        recs.append("Bảo dưỡng/thay pin (SoH trung bình " + (int)Math.round(s.avgSoH) + "% )");
                        if (evidence.length()>0) evidence.append(" ");
                        evidence.append("SoH trung bình: ").append((int)Math.round(s.avgSoH)).append("%.");
                    }

                    if (recs.length() == 0) {
                        s.recommendation = "Không cần nâng cấp ngay";
                        s.evidence = "Năng lực hiện tại đáp ứng nhu cầu gần đây";
                    } else {
                        s.recommendation = recs.toString();
                        s.evidence = evidence.toString();
                    }

                    outList.add(s);
                }
            }
        } catch (Exception ex) {
            out.println("<div class='muted'>Lỗi khi truy vấn dữ liệu: " + ex.getMessage() + "</div>");
        }
    %>

    <table id="suggestions">
        <thead>
            <tr>
                <th>ChargingStation ID</th>
                <th>Station ID</th>
                <th>Tên</th>
                <th>Slot Capacity</th>
                <th>Gợi ý/Loại nâng cấp</th>
                <th>Bằng chứng</th>
                <th>Số liệu tóm tắt</th>
            </tr>
        </thead>
        <tbody>
        <%
            for (Suggestion s : outList) {
                String cls = (s.recommendation != null && !s.recommendation.equals("Không cần nâng cấp ngay")) ? "warn" : "ok";
        %>
            <tr class="<%= cls %>">
                <td><%= s.csId %></td>
                <td><%= s.stationId %></td>
                <td><%= s.name == null ? "" : s.name %></td>
                <td><%= s.slotCap %></td>
                <td><%= s.recommendation %></td>
                <td><%= s.evidence %></td>
                <td>
                    last7Avg: <%= Math.round(s.last7Avg*10)/10.0 %><br/>
                    prev7Avg: <%= Math.round(s.prev7Avg*10)/10.0 %><br/>
                    overloadDays: <%= s.overloadDays %><br/>
                    failRate: <%= (int)Math.round(s.failRate*100) %>%
                </td>
            </tr>
        <%
            }
        %>
        </tbody>
    </table>

</body>
</html>
