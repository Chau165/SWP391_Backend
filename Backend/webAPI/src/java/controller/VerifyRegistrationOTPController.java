package controller;

import DAO.RegistrationOtpDAO;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/verify-registration-otp")
public class VerifyRegistrationOtpController extends HttpServlet {
    private final RegistrationOtpDAO registrationOtpDAO = new RegistrationOtpDAO();
    private final Gson gson = new Gson();
    private final DAO.UsersDAO usersDAO = new DAO.UsersDAO();

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
            VerifyRegistrationOtpRequest req = gson.fromJson(sb.toString(), VerifyRegistrationOtpRequest.class);
            if (req == null || req.email == null || req.otp == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Email và OTP không được để trống\"}");
                return;
            }
            if (req.otp.length() != 6) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"OTP phải có 6 chữ số\"}");
                return;
            }
            boolean isValid = registrationOtpDAO.verifyOtp(req.email, req.otp);
            if (isValid) {
                // If the email corresponds to an existing user whose status is not Active,
                // treat this verification as an activation flow: set Status='Active' and mark OTP used.
                DTO.Users existing = usersDAO.getUserByEmail(req.email);
                if (existing != null) {
                    String cur = existing.getStatus();
                    if (cur == null || !cur.equalsIgnoreCase("Active")) {
                        boolean updated = usersDAO.updateStatusByEmail(req.email, "Active");
                        if (updated) {
                            // mark OTP used for activation
                            registrationOtpDAO.markOtpAsUsed(req.email, req.otp);
                            response.setStatus(HttpServletResponse.SC_OK);
                            out.print("{\"status\":\"success\",\"message\":\"Tài khoản đã được kích hoạt\"}");
                            System.out.println("[VerifyRegistrationOtpController] Activated account for: " + req.email);
                            return;
                        } else {
                            // failed to update status
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            out.print("{\"status\":\"error\",\"message\":\"Không thể cập nhật trạng thái tài khoản\"}");
                            return;
                        }
                    }
                }

                // Otherwise, OTP is valid (registration flow). Don't mark used here; registerController will do it.
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"status\":\"success\",\"message\":\"OTP hợp lệ\"}");
                System.out.println("[VerifyRegistrationOtpController] OTP verified for: " + req.email + " (registration flow)");
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"status\":\"fail\",\"message\":\"OTP không hợp lệ hoặc đã hết hạn\"}");
            }
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

    private static class VerifyRegistrationOtpRequest {
        String email;
        String otp;
    }
}
