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
import java.sql.Timestamp;

@WebServlet("/api/resend-verification")
public class ResendVerificationController extends HttpServlet {
    private final UsersDAO usersDAO = new UsersDAO();
    private final RegistrationOtpDAO registrationOtpDAO = new RegistrationOtpDAO();
    private final Gson gson = new Gson();

    // cooldown in milliseconds (1 minute)
    private static final long COOLDOWN_MS = 60 * 1000L;

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
            ResendRequest req = gson.fromJson(sb.toString(), ResendRequest.class);
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

            // Ensure user exists and is not Active
            DTO.Users existing = usersDAO.getUserByEmail(req.email);
            if (existing == null) {
                // No user yet; the frontend should direct them to register instead
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"status\":\"fail\",\"message\":\"Không tìm thấy tài khoản. Vui lòng đăng ký.\"}");
                return;
            }
            if (existing.getStatus() != null && existing.getStatus().equalsIgnoreCase("Active")) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print("{\"status\":\"fail\",\"message\":\"Tài khoản đã được kích hoạt\"}");
                return;
            }

            // Rate limit: check last OTP creation time
            Timestamp last = registrationOtpDAO.getLastOtpCreatedAt(req.email);
            if (last != null) {
                long since = System.currentTimeMillis() - last.getTime();
                if (since < COOLDOWN_MS) {
                    long secondsLeft = (COOLDOWN_MS - since + 999) / 1000;
                    // Some servlet API versions don't define SC_TOO_MANY_REQUESTS constant; use numeric 429
                    response.setStatus(429);
                    out.print("{\"status\":\"fail\",\"message\":\"Vui lòng đợi " + secondsLeft + " giây trước khi gửi lại email kích hoạt.\"}");
                    return;
                }
            }

            // Generate, save and send OTP
            String otp = EmailService.generateOtp();
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
            out.print("{\"status\":\"success\",\"message\":\"Verification email has been resent to your email. Please check your inbox.\"}");
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

    private static class ResendRequest {
        String email;
    }
}
