package controller;

import DAO.PasswordResetDAO;
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

/**
 * ForgotPasswordController - API nhận email và gửi OTP
 * Endpoint: POST /api/forgot-password
 */
@WebServlet("/api/forgot-password")
public class ForgotPasswordController extends HttpServlet {

    private final UsersDAO usersDAO = new UsersDAO();
    private final PasswordResetDAO passwordResetDAO = new PasswordResetDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = response.getWriter();
             BufferedReader reader = request.getReader()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            ForgotPasswordRequest req = gson.fromJson(sb.toString(), ForgotPasswordRequest.class);

            // Validate input
            if (req == null || req.email == null || req.email.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Email không được để trống\"}");
                return;
            }

            // Validate email format
            if (!mylib.ValidationUtil.isValidEmail(req.email)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Email không hợp lệ\"}");
                return;
            }

            // Kiểm tra email có tồn tại trong hệ thống không
            if (!usersDAO.existsByEmail(req.email)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"status\":\"fail\",\"message\":\"Email không tồn tại trong hệ thống\"}");
                return;
            }

            // Tạo OTP 6 chữ số
            String otp = EmailService.generateOtp();
            System.out.println("[ForgotPasswordController] Generated OTP: " + otp + " for email: " + req.email);

            // Lưu OTP vào database
            boolean saved = passwordResetDAO.saveOtp(req.email, otp);
            System.out.println("[ForgotPasswordController] OTP save result: " + saved);
            if (!saved) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"Không thể lưu OTP\"}");
                return;
            }

            // Gửi OTP qua email
            System.out.println("[ForgotPasswordController] Attempting to send email to: " + req.email);
            boolean sent = EmailService.sendOtpEmail(req.email, otp);
            System.out.println("[ForgotPasswordController] Email send result: " + sent);
            if (!sent) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"Không thể gửi email. Vui lòng thử lại sau\"}");
                return;
            }

            // Thành công
            response.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"status\":\"success\",\"message\":\"OTP đã được gửi đến email của bạn\"}");
            System.out.println("[ForgotPasswordController] Complete! OTP sent successfully to: " + req.email);
            System.out.println("[ForgotPasswordController] OTP sent to: " + req.email);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"Lỗi server: "
                        + e.getMessage().replace("\"", "'") + "\"}");
            }
            e.printStackTrace();
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static class ForgotPasswordRequest {
        String email;
    }
}
