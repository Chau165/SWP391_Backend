package controller;

import DAO.PasswordResetDAO;
import DAO.UsersDAO;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * ResetPasswordController - API đổi mật khẩu mới sau khi xác thực OTP
 * Endpoint: POST /api/reset-password
 */
@WebServlet("/api/reset-password")
public class ResetPasswordController extends HttpServlet {

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

            ResetPasswordRequest req = gson.fromJson(sb.toString(), ResetPasswordRequest.class);

            // Validate input
            if (req == null || req.email == null || req.otp == null || req.newPassword == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Thiếu thông tin bắt buộc\"}");
                return;
            }

            if (req.newPassword.length() < 6) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Mật khẩu phải có ít nhất 6 ký tự\"}");
                return;
            }

            // Xác thực OTP lần cuối trước khi đổi mật khẩu
            boolean isValidOtp = passwordResetDAO.verifyOtp(req.email, req.otp);
            if (!isValidOtp) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"status\":\"fail\",\"message\":\"OTP không hợp lệ hoặc đã hết hạn\"}");
                return;
            }

            // Cập nhật mật khẩu mới
            boolean updated = usersDAO.updatePassword(req.email, req.newPassword);
            if (!updated) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"Không thể cập nhật mật khẩu\"}");
                return;
            }

            // Đánh dấu OTP đã sử dụng
            passwordResetDAO.markOtpAsUsed(req.email, req.otp);

            // Thành công
            response.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"status\":\"success\",\"message\":\"Đổi mật khẩu thành công\"}");

            System.out.println("[ResetPasswordController] Password reset successfully for: " + req.email);

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

    private static class ResetPasswordRequest {
        String email;
        String otp;
        String newPassword;
    }
}
