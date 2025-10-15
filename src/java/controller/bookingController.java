package controller;

import com.google.gson.*;
import DAO.BookingDAO;
import DAO.BatterySlotDAO;
import DAO.DriverPackageDAO;
import DTO.Booking;
import DTO.BatterySlot;
import DTO.Package;
import utils.QRCodeUtil;
import mylib.DBUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.PrintWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet("/api/secure/booking")
public class bookingController extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            Integer userId = (Integer) request.getAttribute("jwt_id");
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(gson.toJson(error("Unauthorized: missing JWT user id")));
                return;
            }

            JsonObject json = JsonParser.parseReader(request.getReader()).getAsJsonObject();
            String stationName    = json.get("stationName").getAsString();
            String batteryModel   = json.has("batteryModel") ? json.get("batteryModel").getAsString() : "";
            String bookingTimeStr = json.get("bookingTime").getAsString();

            Timestamp bookingTime, expiredTime;
            try {
                // thử ISO
                LocalDateTime dt = LocalDateTime.parse(bookingTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                bookingTime = Timestamp.valueOf(dt);
                expiredTime = Timestamp.valueOf(dt.plusHours(1));
            } catch (Exception isoEx) {
                try {
                    // fallback Việt: "dd/MM/yyyy hh:mm a" (SA/CH)
                    java.util.Locale vi = new java.util.Locale("vi", "VN");
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", vi);
                    LocalDateTime dt2 = LocalDateTime.parse(bookingTimeStr, fmt);
                    bookingTime = Timestamp.valueOf(dt2);
                    expiredTime = Timestamp.valueOf(dt2.plusHours(1));
                } catch (Exception vnEx) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(error("Invalid datetime format. Use ISO or 'dd/MM/yyyy hh:mm SA/CH'")));
                    return;
                }
            }

            BookingDAO bookingDao = new BookingDAO();
            DriverPackageDAO driverPkgDao = new DriverPackageDAO();
            BatterySlotDAO slotDao = new BatterySlotDAO();

            try (Connection con = DBUtils.getConnection()) {
                con.setAutoCommit(false);

                int stationId = bookingDao.getStationIdByName(con, stationName);
                if (stationId == -1) {
                    con.rollback();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(error("Station not found")));
                    return;
                }

                int vehicleId = bookingDao.getVehicleIdByUserId(con, userId);
                if (vehicleId == -1) {
                    con.rollback();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(error("Vehicle not found for current user")));
                    return;
                }

                Package pack = driverPkgDao.getCurrentPackage(con, userId);
                if (pack == null) {
                    con.rollback();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(error("No active package for current user")));
                    return;
                }

                String modelFilter = (batteryModel != null && !batteryModel.trim().isEmpty())
                        ? batteryModel.trim() : "";

                BatterySlot slot = slotDao.findAndReserveSuitableSlot(con, stationId, modelFilter,
                        pack.getMinSoH(), pack.getMaxSoH());
                if (slot == null) {
                    con.rollback();
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(error("No suitable battery found at this station")));
                    return;
                }

                Booking booking = new Booking();
                booking.setUser_ID(userId);
                booking.setVehicle_ID(vehicleId);
                booking.setPackage_ID(pack.getPackageId());
                booking.setStation_ID(stationId);
                booking.setChargingStation_ID(slot.getChargingStation_ID());
                booking.setSlot_ID(slot.getSlot_ID());
                booking.setBattery_Request(modelFilter);
                booking.setStatus("Reserved");
                booking.setBooking_Time(bookingTime);
                booking.setExpired_Date(expiredTime);
                booking.setQr_Code(null);

                int bookingId = bookingDao.insertBooking(con, booking);
                if (bookingId <= 0) {
                    con.rollback();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(error("Failed to create booking")));
                    return;
                }

                String qr = null;
                try {
                    qr = QRCodeUtil.generateQRCodeBase64("BOOK-" + bookingId, 200, 200);
                    bookingDao.updateQRCode(con, bookingId, qr);
                } catch (Exception qrEx) {
                    qrEx.printStackTrace(); // không rollback
                    qr = null;
                }

                con.commit();

                JsonObject resp = new JsonObject();
                resp.addProperty("bookingId", bookingId);
                resp.addProperty("stationId", stationId);
                resp.addProperty("chargingStationId", slot.getChargingStation_ID());
                resp.addProperty("slotId", slot.getSlot_ID());
                resp.addProperty("batteryModel", modelFilter);
                resp.addProperty("status", "Reserved");
                resp.addProperty("bookingTime", bookingTime.toString());
                resp.addProperty("expiredTime", expiredTime.toString());
                if (qr != null) resp.addProperty("qrCode", qr);
                else resp.add("qrCode", JsonNull.INSTANCE);

                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(resp));
            } catch (Exception ex) {
                ex.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                try (PrintWriter pw = response.getWriter()) {
                    pw.print(gson.toJson(error("Internal server error: " + ex.getMessage())));
                }
            }
        }
    }

    private JsonObject error(String msg) {
        JsonObject o = new JsonObject();
        o.addProperty("error", msg);
        return o;
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
