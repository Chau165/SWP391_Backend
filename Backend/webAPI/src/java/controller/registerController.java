package controller;

import DAO.RegistrationOtpDAO;
import DAO.UsersDAO;
import DAO.UserProfileDAO;
import DTO.Users;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mylib.ValidationUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/register")
public class registerController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try (BufferedReader reader = req.getReader();
             PrintWriter out = resp.getWriter()) {

            Gson gson = new Gson();
            RegisterRequest input = gson.fromJson(reader, RegisterRequest.class);

            if (input == null) {
                resp.setStatus(400);
                out.print("{\"error\":\"Invalid input\"}");
                return;
            }

            // Validate full name
            if (!ValidationUtil.isValidFullName(input.fullName)) {
                resp.setStatus(400);
                out.print("{\"error\":\"Full name is invalid\"}");
                return;
            }

            // validate phone (Vietnam)
            if (!ValidationUtil.isValidVNPhone(input.phone)) {
                resp.setStatus(400);
                out.print("{\"error\":\"Phone number is not a valid VN mobile number\"}");
                return;
            }

            // validate email
            if (!ValidationUtil.isValidEmail(input.email)) {
                resp.setStatus(400);
                out.print("{\"error\":\"Email is invalid\"}");
                return;
            }

            // validate password
            if (!ValidationUtil.isValidPassword(input.password)) {
                resp.setStatus(400);
                out.print("{\"error\":\"Password must be at least 6 characters, include letters and digits\"}");
                return;
            }

            // ✅ BƯỚC MỚI: Xác thực OTP trước khi đăng ký
            if (input.otp == null || input.otp.trim().isEmpty()) {
                resp.setStatus(400);
                out.print("{\"error\":\"OTP is required for registration\"}");
                return;
            }

            if (input.otp.length() != 6) {
                resp.setStatus(400);
                out.print("{\"error\":\"OTP must be 6 digits\"}");
                return;
            }

            // Kiểm tra OTP có hợp lệ không
            RegistrationOtpDAO registrationOtpDAO = new RegistrationOtpDAO();
            boolean isOtpValid = registrationOtpDAO.verifyOtp(input.email, input.otp);
            
            if (!isOtpValid) {
                resp.setStatus(401);
                out.print("{\"error\":\"OTP is invalid or expired. Please request a new OTP\"}");
                System.out.println("[registerController] Registration failed: Invalid OTP for email " + input.email);
                return;
            }

            System.out.println("[registerController] OTP verified successfully for email: " + input.email + " (not marked as used yet)");

            // Tiếp tục quy trình đăng ký như cũ
            UsersDAO dao = new UsersDAO();
            if (dao.existsByEmail(input.email)) {
                resp.setStatus(409);
                out.print("{\"error\":\"Email already exists\"}");
                return;
            }

            // Tạo Users object
            Users user = new Users();
            user.setFullName(input.fullName);
            user.setPhone(input.phone);
            user.setEmail(input.email);
            user.setPassword(input.password);
            user.setRole("Driver");
            user.setStationId(null);

            // Thêm user vào database
            int newId = dao.insertUser(user);

            if (newId > 0) {
                // ✅ CHỈ mark OTP as used SAU KHI tạo user THÀNH CÔNG
                registrationOtpDAO.markOtpAsUsed(input.email, input.otp);
                System.out.println("[registerController] User registered successfully with ID: " + newId + ", OTP marked as used");
                
                // Tạo profile cho user mới
                UserProfileDAO profileDAO = new UserProfileDAO();
                profileDAO.createOrUpdateProfile(newId, input.fullName, input.email, input.phone, "Driver");
                System.out.println("[registerController] Profile created for new user ID: " + newId);
                
                resp.setStatus(201);
                out.print("{\"status\":\"success\",\"userId\":" + newId + ",\"role\":\"Driver\"}");
            } else {
                resp.setStatus(500);
                out.print("{\"error\":\"Failed to create user\"}");
                System.out.println("[registerController] Registration failed: Could not insert user");
            }
        } catch (Exception e) {
            resp.setStatus(500);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"error\":\"Server error: " + e.getMessage().replace("\"", "'") + "\"}");
            }
            e.printStackTrace();
        }
    }

    /**
     * Request class cho đăng ký - bao gồm OTP
     */
    private static class RegisterRequest {
        String fullName;
        String phone;
        String email;
        String password;
        String otp;  // ✅ Thêm field OTP
    }
}
