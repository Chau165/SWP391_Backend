package controller;

import DAO.DriverPackageDAO;
import DAO.PaymentTransactionDAO;
import DTO.DriverPackage;
import DTO.PaymentTransaction;
import config.VnPayConfig;
import config.VnPayUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/api/buyPackage")
public class buyPackageController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");

        try {
            // ===== Lấy tất cả params từ VNPay =====
            Map<String, String> vnp_Params = new HashMap<>();
            Map<String, String[]> paramMap = req.getParameterMap();
            for (String key : paramMap.keySet()) {
                String[] values = paramMap.get(key);
                if (values != null && values.length > 0) {
                    vnp_Params.put(key, values[0]);
                }
            }

            // ===== Lấy chữ ký VNPay =====
            String vnp_SecureHash = vnp_Params.get("vnp_SecureHash");
            vnp_Params.remove("vnp_SecureHash");
            vnp_Params.remove("vnp_SecureHashType");

            // ===== Sắp xếp key và tạo chuỗi hashData =====
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();

            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            // ===== Tính chữ ký =====
            String signValue = VnPayUtil.hmacSHA512(VnPayConfig.vnp_HashSecret, hashData.toString());

            // ===== Kiểm tra chữ ký =====
            if (!signValue.equals(vnp_SecureHash)) {
                resp.getWriter().println("❌ Chữ ký không hợp lệ!");
                return;
            }

            // ===== Kiểm tra mã phản hồi =====
            String responseCode = vnp_Params.get("vnp_ResponseCode");
            if (!"00".equals(responseCode)) {
                resp.getWriter().println("❌ Thanh toán thất bại! Mã lỗi: " + responseCode);
                return;
            }

            // ===== Lấy thông tin order =====
            String orderInfo = URLDecoder.decode(vnp_Params.get("vnp_OrderInfo"), "UTF-8");
            Map<String, String> infoMap = parseOrderInfo(orderInfo);
            int userId = Integer.parseInt(infoMap.get("userId"));
            int packageId = Integer.parseInt(infoMap.get("packageId"));

            // ===== Tạo đối tượng DriverPackage =====
            LocalDate start = LocalDate.now();
            LocalDate end = start.plusDays(30);
            DriverPackage dp = new DriverPackage();
            dp.setUser_ID(userId);
            dp.setPackage_ID(packageId);
            dp.setStart_date(Date.valueOf(start));
            dp.setEnd_date(Date.valueOf(end));

            // ===== Ghi vào database (Insert / Update) =====
            DriverPackageDAO dao = new DriverPackageDAO();
            boolean success;

            if (dao.existsDriverPackage(userId)) {
                success = dao.updateDriverPackage(dp);
                if (success) {
                    insertPaymentRecord(userId, packageId, vnp_Params);
                    resp.getWriter().println("🔄 Cập nhật thành công! Gói " + packageId + " cho User " + userId);
                } else {
                    resp.getWriter().println("⚠️ Thanh toán thành công nhưng cập nhật DB thất bại!");
                }
            } else {
                success = dao.insertDriverPackage(dp);
                if (success) {
                    insertPaymentRecord(userId, packageId, vnp_Params);
                    resp.getWriter().println("✅ Thanh toán thành công! Gói " + packageId + " cho User " + userId);
                } else {
                    resp.getWriter().println("⚠️ Thanh toán thành công nhưng không lưu DB!");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().println("⚠️ Lỗi xử lý thanh toán: " + e.getMessage());
        }
    }

    // ===== Hàm ghi thanh toán vào DB =====
    private void insertPaymentRecord(int userId, int packageId, Map<String, String> vnp_Params) {
        PaymentTransaction payment = new PaymentTransaction();
        payment.setUser_ID(userId);
        payment.setStation_ID(null); // không liên quan đến trạm
        payment.setPackage_ID(packageId);
        payment.setAmount(Double.parseDouble(vnp_Params.get("vnp_Amount")) / 100);
        payment.setPayment_Method("VNPay");
        payment.setDescription("Buy Battery Package");
        payment.setTransaction_Time(new Timestamp(System.currentTimeMillis()));

        PaymentTransactionDAO paymentDAO = new PaymentTransactionDAO();
        paymentDAO.insertPayment(payment);
    }

    // ===== Hàm parse orderInfo =====
    private Map<String, String> parseOrderInfo(String orderInfo) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = orderInfo.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}
