package controller;

import DAO.*;
import DTO.*;
import config.VnPayConfigSwap;
import config.VnPayUtil;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@WebServlet("/api/checkin")
public class checkInController extends HttpServlet {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final PackageDAO packageDAO = new PackageDAO();
    private final BatteryDAO batteryDAO = new BatteryDAO();
    private final BatterySlotDAO slotDAO = new BatterySlotDAO();
    private final SwapTransactionDAO swapDAO = new SwapTransactionDAO();
    private final PaymentTransactionDAO paymentDAO = new PaymentTransactionDAO();
    private final Random rng = new Random();
    private final Gson gson = new Gson();

    private static final double NOMINAL_LI = 0.05;
    private static final double NOMINAL_LFP = 0.03;

    // ========================== VNPay ReturnUrl (GET) ==========================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        try {
            Map<String, String> vnp_Params = new HashMap<>();
            req.getParameterMap().forEach((k, v) -> {
                if (v != null && v.length > 0) {
                    vnp_Params.put(k, v[0]);
                }
            });

            // Verify HMAC
            String vnp_SecureHash = vnp_Params.get("vnp_SecureHash");
            vnp_Params.remove("vnp_SecureHash");
            vnp_Params.remove("vnp_SecureHashType");

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=')
                            .append(java.net.URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
            String signValue = VnPayUtil.hmacSHA512(VnPayConfigSwap.vnp_HashSecret, hashData.toString());
            if (!signValue.equals(vnp_SecureHash)) {
                resp.getWriter().println("❌ Chữ ký không hợp lệ!");
                return;
            }

            if (!"00".equals(vnp_Params.get("vnp_ResponseCode"))) {
                resp.getWriter().println("❌ Thanh toán thất bại! Mã lỗi: " + vnp_Params.get("vnp_ResponseCode"));
                return;
            }

            // paymentId từ vnp_TxnRef (SWAP-<id>)
            String txnRef = vnp_Params.get("vnp_TxnRef");
            int paymentId = parseDigits(txnRef);
            if (paymentId <= 0) {
                resp.getWriter().println("⚠️ TxnRef không hợp lệ: " + txnRef);
                return;
            }

            double amount = Double.parseDouble(vnp_Params.get("vnp_Amount")) / 100.0;
            PaymentTransaction payment = paymentDAO.getPaymentById(paymentId);
            if (payment == null) {
                resp.getWriter().println("⚠️ Không tìm thấy PaymentTransaction với ID=" + paymentId);
                return;
            }
            paymentDAO.updatePaymentStatus(paymentId, "Success");

            // Decode context
            String oiRaw = vnp_Params.get("vnp_OrderInfo");
            String oi = (oiRaw != null) ? URLDecoder.decode(oiRaw, StandardCharsets.UTF_8.name()) : "";
            if (!oi.startsWith("SWAP:")) {
                resp.getWriter().println("⚠️ OrderInfo không đúng định dạng: " + oi);
                return;
            }
            String contextRaw;
            try {
                byte[] decoded = Base64.getDecoder().decode(oi.substring(5));
                contextRaw = new String(decoded, StandardCharsets.UTF_8);
            } catch (Exception e) {
                resp.getWriter().println("⚠️ Lỗi decode Base64: " + e.getMessage());
                return;
            }

            // b=11|s=3|nb=27|so=50.00|rq=75.00|f=500000|m=Lithium-ion|cs=1
            Map<String, String> info = parseContext(contextRaw);
            int bookingId = safeParseInt(info.get("b"), -1);
            int slotId = safeParseInt(info.get("s"), -1);
            int newBatteryId = safeParseInt(info.get("nb"), -1);
            double sohOld = safeParseDouble(info.get("so"), 0.0);
            double required = safeParseDouble(info.get("rq"), 0.0);
            double feePaid = safeParseDouble(info.get("f"), 0.0);
            String model = info.getOrDefault("m", "Lithium-ion");
            int chargingStationId = safeParseInt(info.get("cs"), 0);

            if (bookingId <= 0 || slotId <= 0 || newBatteryId <= 0) {
                resp.getWriter().println("⚠️ Thiếu ngữ cảnh swap. Context: " + contextRaw);
                return;
            }

            Battery newBattery = batteryDAO.getBatteryById(newBatteryId);
            double sohNew = (newBattery != null) ? newBattery.getSoH() : 0.0;

            // Tạo pin cũ và cập nhật slot
            Battery oldBat = new Battery();
            oldBat.setSerialNumber("OLD-" + System.currentTimeMillis());
            oldBat.setSoH(sohOld);
            oldBat.setResistance(calcResistance(model, sohOld));
            oldBat.setTypeId(resolveBatteryTypeId(model));
            int oldBatteryId = batteryDAO.insertBattery(oldBat);

            slotDAO.removeBatteryFromSlot(slotId);
            if (oldBatteryId > 0) {
                String condition = (sohOld >= 70) ? "Weak" : "Damage";
                slotDAO.assignBatteryToSlot(slotId, oldBatteryId, condition);
            }
            batteryDAO.deleteBattery(newBatteryId);

            // Lấy station/CS từ booking (fallback: payment.Station_ID)
            Booking booking = bookingDAO.getBookingById(bookingId);
            int stationId = (booking != null) ? booking.getStation_ID() : payment.getStation_ID();
            int csId = (chargingStationId > 0) ? chargingStationId
                    : (booking != null ? booking.getChargingStation_ID() : 0);

            // Ghi SwapTransaction
            SwapTransaction tx = new SwapTransaction();
            tx.setDriver_ID(payment.getUser_ID());
            tx.setStaff_ID(0);
            tx.setStation_ID(stationId);
            tx.setChargingStation_ID(csId);
            tx.setOld_Battery(oldBatteryId);
            tx.setNew_Battery(newBatteryId);
            tx.setSoH_Old(sohOld);
            tx.setSoH_New(sohNew);
            tx.setFee(feePaid);
            tx.setPayment_ID(paymentId);
            tx.setStatus("Completed");
            tx.setSwap_Time(new Timestamp(System.currentTimeMillis()));
            tx.setBooking_ID(bookingId);

            int transactionId = swapDAO.insertSwapTransaction(tx);
            bookingDAO.updateStatus(bookingId, "Completed");

            resp.getWriter().println(
                    "✅ Thanh toán & đổi pin thành công!\n"
                    + "Payment ID: " + paymentId + "\n"
                    + "Booking: " + bookingId + "\n"
                    + "Transaction ID: " + transactionId + "\n"
                    + "Old Battery: " + oldBatteryId + "\n"
                    + "New Battery: " + newBatteryId + "\n"
                    + "Amount: " + amount + " VND"
            );

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().println("⚠️ Lỗi xử lý thanh toán: " + e.getMessage());
        }
    }

    // ========================== Check-in (POST) ==========================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String bookingIdStr = req.getParameter("bookingId");
            if (bookingIdStr == null || bookingIdStr.isEmpty()) {
                out.print(gson.toJson(error("Thiếu bookingId")));
                return;
            }
            int bookingId = Integer.parseInt(bookingIdStr);

            Booking booking = bookingDAO.getBookingById(bookingId);
            if (booking == null) {
                out.print(gson.toJson(error("Booking không tồn tại")));
                return;
            }
            if (!"Reserved".equalsIgnoreCase(booking.getStatus())) {
                out.print(gson.toJson(error("Booking không hợp lệ hoặc đã check-in")));
                return;
            }
            if (booking.getExpired_Date().before(new Timestamp(System.currentTimeMillis()))) {
                out.print(gson.toJson(error("Booking đã hết hạn")));
                return;
            }

            int driverId = booking.getUser_ID();
            int stationId = booking.getStation_ID();
            int chargingStationId = booking.getChargingStation_ID();
            String modelRequested = booking.getBattery_Request();
            int packageId = booking.getPackage_ID();
            int slotId = booking.getSlot_ID();

            DTO.Package pkg = packageDAO.getPackageById(packageId);
            if (pkg == null) {
                out.print(gson.toJson(error("Package không tồn tại")));
                return;
            }
            double requiredSoH = pkg.getRequiredSoH();

            BatterySlot reservedSlot = slotDAO.getSlotById(slotId);
            if (reservedSlot == null
                    || !"Reserved".equalsIgnoreCase(reservedSlot.getState())
                    || !"Good".equalsIgnoreCase(reservedSlot.getCondition())) {
                out.print(gson.toJson(error("Slot không khả dụng")));
                return;
            }

            Battery newBattery = batteryDAO.getBatteryById(reservedSlot.getBattery_ID());
            if (newBattery == null || newBattery.getBatteryId() == 0) {
                out.print(gson.toJson(error("Pin mới không tồn tại hoặc chưa có ID")));
                return;
            }
            int newBatteryId = newBattery.getBatteryId();
            double newBatterySoH = newBattery.getSoH();

            // Ước lượng SoH cũ
            SwapTransaction lastSwap = swapDAO.getLastSwapByDriverId(driverId);
            double sohOld;
            LocalDateTime now = LocalDateTime.now();
            if (lastSwap != null && lastSwap.getSoH_New() > 0 && lastSwap.getSwap_Time() != null) {
                long minutes = Math.max(1, Duration.between(lastSwap.getSwap_Time().toLocalDateTime(), now).toMinutes());
                sohOld = Math.max(50.0, lastSwap.getSoH_New() - minutes * 0.7);
            } else {
                sohOld = 65.0 + rng.nextDouble() * (95.0 - 65.0);
            }
            sohOld = Math.round(sohOld * 100.0) / 100.0;

            // Tính phí
            double fee = Math.max(0, Math.round((requiredSoH - sohOld) * 20000.0));
            boolean free = fee == 0;

            // ===== Luôn insert PaymentTransaction, dù free hay có phí =====
            PaymentTransaction payment = new PaymentTransaction();
            payment.setUser_ID(driverId);
            payment.setStation_ID(stationId);
            payment.setPackage_ID(null);
            payment.setAmount(fee);
            payment.setPayment_Method(free ? "Free" : "VNPay");
            payment.setDescription("Swap Battery" + (free ? " (Miễn phí)" : ""));
            payment.setTransaction_Time(new Timestamp(System.currentTimeMillis()));

            int paymentId = paymentDAO.insertPayment(payment);
            if (paymentId <= 0) {
                out.print(gson.toJson(error("Không tạo được PaymentTransaction")));
                return;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("bookingId", bookingId);
            result.put("driverId", driverId);
            result.put("stationId", stationId);
            result.put("chargingStationId", chargingStationId);
            result.put("sohOld", sohOld);
            result.put("requiredSoH", requiredSoH);
            result.put("fee", fee);
            result.put("paymentId", paymentId);

            if (!free) {
                // Tạo payment URL VNPay
                String txnRef = "SWAP-" + paymentId;
                String contextRaw = String.format(
                        Locale.US,
                        "b=%d|s=%d|nb=%d|so=%.2f|rq=%.2f|f=%.0f|m=%s|cs=%d",
                        bookingId, slotId, newBatteryId, sohOld, requiredSoH, fee,
                        normalizeModel(modelRequested), chargingStationId
                );
                String orderInfo = "SWAP:" + Base64.getEncoder().encodeToString(
                        contextRaw.getBytes(StandardCharsets.UTF_8)
                );
                String paymentUrl = VnPayUtil.createSwapPaymentUrl(req, txnRef, Math.round(fee), orderInfo);

                result.put("txnRef", txnRef);
                result.put("paymentUrl", paymentUrl);
                result.put("message", "SoH pin < Required_SoH, cần thanh toán trước khi đổi pin");
                out.print(gson.toJson(result));
                return;
            }

            // Miễn phí → swap ngay
            Battery oldBat = new Battery();
            oldBat.setSerialNumber("OLD-" + System.currentTimeMillis());
            oldBat.setSoH(sohOld);
            oldBat.setResistance(calcResistance(modelRequested, sohOld));
            oldBat.setTypeId(resolveBatteryTypeId(modelRequested));
            int oldBatteryId = batteryDAO.insertBattery(oldBat);

            slotDAO.removeBatteryFromSlot(slotId);
            if (oldBatteryId > 0) {
                String condition = (sohOld >= 70) ? "Weak" : "Damage";
                slotDAO.assignBatteryToSlot(slotId, oldBatteryId, condition);
            }
            batteryDAO.deleteBattery(newBatteryId);

            SwapTransaction tx = new SwapTransaction();
            tx.setDriver_ID(driverId);
            tx.setStaff_ID(0);
            tx.setStation_ID(stationId);
            tx.setChargingStation_ID(chargingStationId);
            tx.setOld_Battery(oldBatteryId);
            tx.setNew_Battery(newBatteryId);
            tx.setSoH_Old(sohOld);
            tx.setSoH_New(newBatterySoH);
            tx.setFee(fee);       // vẫn lưu fee = 0
            tx.setPayment_ID(paymentId);  // liên kết đến PaymentTransaction
            tx.setStatus("Completed");
            tx.setSwap_Time(new Timestamp(System.currentTimeMillis()));
            tx.setBooking_ID(bookingId);

            int transactionId = swapDAO.insertSwapTransaction(tx);
            bookingDAO.updateStatus(bookingId, "Completed");

            result.put("message", "Đổi pin miễn phí");
            result.put("transactionId", transactionId);
            result.put("oldBatteryId", oldBatteryId);
            result.put("newBatteryId", newBatteryId);
            result.put("sohNew", newBatterySoH);
            out.print(gson.toJson(result));

        } catch (Exception ex) {
            ex.printStackTrace();
            out.print(gson.toJson(error("Lỗi xử lý: " + ex.getMessage())));
        }
    }

    // ========================== Helpers ==========================
    private Map<String, Object> error(String msg) {
        Map<String, Object> err = new HashMap<>();
        err.put("error", msg);
        return err;
    }

    private String normalizeModel(String modelRequested) {
        if (modelRequested == null) {
            return "Lithium-ion";
        }
        String s = modelRequested.toLowerCase();
        if (s.contains("lfp")) {
            return "LFP";
        }
        if (s.contains("lithium")) {
            return "Lithium-ion";
        }
        return "Lithium-ion";
    }

    private double calcResistance(String model, double sohOld) {
        double nominal = (normalizeModel(model).equals("LFP")) ? NOMINAL_LFP : NOMINAL_LI;
        return sohOld > 0 ? Math.round(nominal * 100.0 / sohOld * 1_000_000.0) / 1_000_000.0 : nominal;
    }

    private int resolveBatteryTypeId(String model) {
        return "LFP".equals(normalizeModel(model)) ? 2 : 1; // map cứng
    }

    private Map<String, String> parseContext(String s) {
        Map<String, String> map = new HashMap<>();
        if (s == null || s.isEmpty()) {
            return map;
        }
        for (String p : s.split("\\|")) {
            int i = p.indexOf('=');
            if (i > 0 && i < p.length() - 1) {
                map.put(p.substring(0, i).trim(), p.substring(i + 1).trim());
            }
        }
        return map;
    }

    private int safeParseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private double safeParseDouble(String s, double def) {
        try {
            if (s == null) {
                return def;
            }
            return Double.parseDouble(s.replace(',', '.').trim());
        } catch (Exception e) {
            return def;
        }
    }

    private int parseDigits(String s) {
        if (s == null) {
            return -1;
        }
        String d = s.replaceAll("\\D+", "");
        try {
            return Integer.parseInt(d);
        } catch (Exception e) {
            return -1;
        }
    }
}
