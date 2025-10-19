package controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/api/verify-registration-otp")
public class VerifyRegistrationOTPController extends HttpServlet {

    private static final long OTP_VALIDITY_MS = 5 * 60 * 1000; // 5 minutes

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        try (BufferedReader reader = request.getReader();
             PrintWriter out = response.getWriter()) {

            Gson gson = new Gson();
            Map<String, String> requestData = gson.fromJson(reader, Map.class);
            String email = requestData.get("email");
            String inputOtp = requestData.get("otp");

            if (email == null || email.trim().isEmpty()) {
                response.setStatus(400);
                out.print("{\"error\":\"Email không được để trống\"}");
                return;
            }

            if (inputOtp == null || inputOtp.trim().isEmpty()) {
                response.setStatus(400);
                out.print("{\"error\":\"Mã OTP không được để trống\"}");
                return;
            }

            HttpSession session = request.getSession();
            String storedOtp = (String) session.getAttribute("registration_otp_" + email);
            Long otpTime = (Long) session.getAttribute("registration_otp_time_" + email);

            if (storedOtp == null || otpTime == null) {
                response.setStatus(400);
                out.print("{\"error\":\"Không tìm thấy mã OTP. Vui lòng yêu cầu gửi lại.\"}");
                return;
            }

            // Check OTP expiration
            if (System.currentTimeMillis() - otpTime > OTP_VALIDITY_MS) {
                session.removeAttribute("registration_otp_" + email);
                session.removeAttribute("registration_otp_time_" + email);
                response.setStatus(400);
                out.print("{\"error\":\"Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại.\"}");
                return;
            }

            // Verify OTP
            if (storedOtp.equals(inputOtp.trim())) {
                // OTP is correct, remove from session
                session.removeAttribute("registration_otp_" + email);
                session.removeAttribute("registration_otp_time_" + email);
                
                response.setStatus(200);
                out.print("{\"status\":\"success\",\"message\":\"Xác thực thành công\"}");
            } else {
                response.setStatus(400);
                out.print("{\"error\":\"Mã OTP không đúng. Vui lòng thử lại.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Lỗi server: " + e.getMessage() + "\"}");
            }
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(200);
    }
}
