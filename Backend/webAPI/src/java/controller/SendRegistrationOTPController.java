package controller;

import mylib.EmailService;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@WebServlet("/api/send-registration-otp")
public class SendRegistrationOTPController extends HttpServlet {

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

            if (email == null || email.trim().isEmpty()) {
                response.setStatus(400);
                out.print("{\"error\":\"Email không được để trống\"}");
                return;
            }

            // Generate 6-digit OTP
            String otp = String.format("%06d", new Random().nextInt(999999));

            // Store OTP in session
            HttpSession session = request.getSession();
            session.setAttribute("registration_otp_" + email, otp);
            session.setAttribute("registration_otp_time_" + email, System.currentTimeMillis());

            // Send OTP via email
            EmailService emailService = new EmailService();
            boolean sent = emailService.sendRegistrationOTP(email, otp);

            if (sent) {
                response.setStatus(200);
                out.print("{\"status\":\"success\",\"message\":\"OTP đã được gửi tới email của bạn\"}");
            } else {
                response.setStatus(500);
                out.print("{\"error\":\"Không thể gửi email. Vui lòng thử lại sau.\"}");
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
