package controller;

import com.google.gson.*;
import mylib.DBUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.PrintWriter;
import java.io.IOException;
import java.sql.*;

@WebServlet("/api/secure/getBookings")
public class getBookingsController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setCorsHeaders(response);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            out.write("[]");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        setCorsHeaders(response);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        PrintWriter out = response.getWriter();
        try {
            Integer jwtUserId = (Integer) request.getAttribute("jwt_id");
            String jwtRole    = (String) request.getAttribute("jwt_role");

            if (jwtUserId == null || jwtRole == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(jsonError("Unauthorized: missing JWT context"));
                out.flush();
                return;
            }

            boolean isPrivileged = "staff".equalsIgnoreCase(jwtRole) || "admin".equalsIgnoreCase(jwtRole);

            // ==== SỬA TRUY VẤN CHO PHÙ HỢP SCHEMA MỚI ====
            final String baseSelect =
                "SELECT b.Booking_ID, b.User_ID, b.Vehicle_ID, b.Package_ID, " +
                "       b.Station_ID, b.ChargingStation_ID, b.Slot_ID, " +
                "       b.Battery_Request, b.Status, b.Booking_Time, b.Expired_Date, b.Qr_Code, " +
                "       u.FullName AS User_Name, " +
                "       v.License_Plate AS Vehicle_License, " +
                "       vm.Model_Name AS Vehicle_ModelName, " +
                "       p.[Name] AS Package_Name, " +
                "       s.Name AS Station_Name, " +
                "       cs.Name AS ChargingStation_Name, " +
                "       bs.Slot_Code " +
                "FROM dbo.Booking b " +
                "JOIN dbo.Users u ON u.ID = b.User_ID " +
                "JOIN dbo.Vehicle v ON v.Vehicle_ID = b.Vehicle_ID " +
                "JOIN dbo.Vehicle_Model vm ON vm.Model_ID = v.Model_ID " +
                "JOIN dbo.Package p ON p.Package_ID = b.Package_ID " +
                "LEFT JOIN dbo.Charging_Station cs ON cs.ChargingStation_ID = b.ChargingStation_ID " +
                "LEFT JOIN dbo.Station s ON s.Station_ID = cs.Station_ID " +
                "LEFT JOIN dbo.BatterySlot bs ON bs.Slot_ID = b.Slot_ID ";

            final String orderBy = " ORDER BY b.Booking_Time DESC";

            String sql = isPrivileged
                    ? baseSelect + orderBy
                    : baseSelect + "WHERE b.User_ID = ? " + orderBy;

            JsonArray arr = new JsonArray();

            try (Connection con = DBUtils.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                if (!isPrivileged) {
                    ps.setInt(1, jwtUserId);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("bookingId", rs.getInt("Booking_ID"));
                        obj.addProperty("userId", rs.getInt("User_ID"));
                        obj.addProperty("vehicleId", rs.getInt("Vehicle_ID"));
                        obj.addProperty("packageId", rs.getInt("Package_ID"));
                        obj.addProperty("stationId", nullSafeInt(rs, "Station_ID"));
                        obj.addProperty("chargingStationId", nullSafeInt(rs, "ChargingStation_ID"));
                        obj.addProperty("slotId", nullSafeInt(rs, "Slot_ID"));

                        // Thông tin hiển thị
                        obj.addProperty("userName", nullSafeStr(rs, "User_Name"));
                        obj.addProperty("vehicleLicense", nullSafeStr(rs, "Vehicle_License"));
                        obj.addProperty("vehicleModel", nullSafeStr(rs, "Vehicle_ModelName"));
                        obj.addProperty("packageName", nullSafeStr(rs, "Package_Name"));
                        obj.addProperty("stationName", nullSafeStr(rs, "Station_Name"));
                        obj.addProperty("chargingStationName", nullSafeStr(rs, "ChargingStation_Name"));
                        obj.addProperty("slotCode", nullSafeStr(rs, "Slot_Code"));

                        // Nội dung booking
                        obj.addProperty("batteryModelRequested", nullSafeStr(rs, "Battery_Request"));
                        obj.addProperty("status", nullSafeStr(rs, "Status"));
                        obj.addProperty("bookingTime", nullSafeTs(rs, "Booking_Time"));
                        obj.addProperty("expiredDate", nullSafeTs(rs, "Expired_Date"));
                        obj.addProperty("qrCode", nullSafeStr(rs, "Qr_Code"));

                        arr.add(obj);
                    }
                }
            }

            // Luôn trả JSON (dù rỗng)
            String payload = gson.toJson(arr);
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(payload);
            out.flush();

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(jsonError("Server error: " + e.getMessage()));
            out.flush();
        }
    }

    // ---------- helpers ----------
    private String jsonError(String msg) {
        JsonObject o = new JsonObject();
        o.addProperty("error", msg);
        return o.toString();
    }

    private String nullSafeStr(ResultSet rs, String col) throws SQLException {
        String v = rs.getString(col);
        return (v == null) ? "" : v;
    }

    private String nullSafeTs(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return (t == null) ? "" : t.toString();
    }

    private Integer nullSafeInt(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
