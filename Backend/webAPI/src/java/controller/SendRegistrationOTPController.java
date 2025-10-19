package controller;

import DAO.RegistrationOtpDAO;
import DAO.UsersDAO;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mylib.EmailService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/send-registration-otp")
public class SendRegistrationOtpController extends HttpServlet {
    private final UsersDAO usersDAO = new UsersDAO();
    private final RegistrationOtpDAO registrationOtpDAO = new RegistrationOtpDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter(); BufferedReader reader = request.getReader()) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            SendRegistrationOtpRequest req = gson.fromJson(sb.toString(), SendRegistrationOtpRequest.class);
            if (req == null || req.email == null || req.email.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Email không được để trống\"}");
                return;
            }
            if (!mylib.ValidationUtil.isValidEmail(req.email)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Email không hợp lệ\"}");
                return;
            }
            if (usersDAO.existsByEmail(req.email)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print("{\"status\":\"fail\",\"message\":\"Email đã được đăng ký trong hệ thống\"}");
                return;
            }
            String otp = EmailService.generateOtp();
            System.out.println("[SendRegistrationOtpController] Generated OTP: " + otp + " for email: " + req.email);
            boolean saved = registrationOtpDAO.saveOtp(req.email, otp);
            if (!saved) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"Không thể lưu OTP\"}");
                return;
            }
            boolean sent = EmailService.sendRegistrationOtpEmail(req.email, otp);
            if (!sent) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"Không thể gửi email. Vui lòng thử lại sau\"}");
                return;
            }
            response.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"status\":\"success\",\"message\":\"OTP đã được gửi đến email của bạn\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"Lỗi server: " + e.getMessage().replace("\"", "'") + "\"}");
            }
            e.printStackTrace();
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static class SendRegistrationOtpRequest {
        String email;
    }
}
