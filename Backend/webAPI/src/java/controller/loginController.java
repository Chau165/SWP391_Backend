package controller;

import DAO.UsersDAO;
import DAO.UserProfileDAO;
import DTO.Users;
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

@WebServlet("/api/login")
public class loginController extends HttpServlet {

    private final UsersDAO usersDAO = new UsersDAO();
    private final UserProfileDAO profileDAO = new UserProfileDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // mirror origin-based CORS behavior (no body for preflight)
        setCorsHeaders(response, request);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    setCorsHeaders(response, request);
        response.setContentType("application/json;charset=UTF-8");

        try ( PrintWriter out = response.getWriter();  BufferedReader reader = request.getReader()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            LoginRequest loginReq = gson.fromJson(sb.toString(), LoginRequest.class);

            if (loginReq == null || loginReq.email == null || loginReq.password == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Missing email or password\"}");
                return;
            }

            // Validate email format
            if (!mylib.ValidationUtil.isValidEmail(loginReq.email)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Email format is invalid\"}");
                return;
            }

            Users user = usersDAO.checkLogin(loginReq.email, loginReq.password);

            if (user != null) {
                // DEBUG: Log user details
                System.out.println("=== LOGIN SUCCESS ===");
                System.out.println("User ID: " + user.getId());
                System.out.println("User Email: " + user.getEmail());
                System.out.println("User Role: " + user.getRole());
                System.out.println("Role length: " + (user.getRole() != null ? user.getRole().length() : "null"));
                System.out.println("=====================");
                
                // Tạo hoặc cập nhật profile cho user
                profileDAO.createOrUpdateProfile(
                    user.getId(), 
                    user.getFullName(), 
                    user.getEmail(), 
                    user.getPhone(), 
                    user.getRole()
                );
                System.out.println("[DEBUG loginController] Profile created/updated for userId=" + user.getId());
                
                // Generate JWT token instead of using server session
                String token = util.JwtUtils.generateToken(user.getEmail(), user.getRole(), user.getId());
                String json = gson.toJson(user);
                System.out.println("DEBUG - JSON response: " + json);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"status\":\"success\",\"token\":\"" + token + "\",\"user\":" + json + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"status\":\"fail\",\"message\":\"Invalid email or password\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try ( PrintWriter out = response.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"Server error: "
                        + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    //private void setCorsHeaders(HttpServletResponse response) {
    //    response.setHeader("Access-Control-Allow-Origin", "*");
    //   response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
    //    response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    //}
  private void setCorsHeaders(HttpServletResponse res, HttpServletRequest req) {
    String origin = req.getHeader("Origin");
    boolean allowed = origin != null && (
            origin.equals("http://localhost:5173") || origin.equals("http://127.0.0.1:5173")
    );
    if (allowed) {
        res.setHeader("Access-Control-Allow-Origin", origin);
        res.setHeader("Access-Control-Allow-Credentials", "true");
    } else {
        res.setHeader("Access-Control-Allow-Origin", "null");
    }
    res.setHeader("Vary", "Origin");
    res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, ngrok-skip-browser-warning");
    res.setHeader("Access-Control-Expose-Headers", "Authorization");
    res.setHeader("Access-Control-Max-Age", "86400");
}

    private static class LoginRequest {

        String email;
        String password;
    }
}
