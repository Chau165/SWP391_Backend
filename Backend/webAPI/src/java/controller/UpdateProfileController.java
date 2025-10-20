package controller;

import DAO.UserProfileDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * API Controller để cập nhật thông tin profile của user
 * Endpoint: /api/profile/update
 * Method: POST
 * Body: { userId, fullName, phone, avatarUrl }
 */
@WebServlet(name = "UpdateProfileController", urlPatterns = {"/api/profile/update"})
public class UpdateProfileController extends HttpServlet {

    private final UserProfileDAO profileDAO = new UserProfileDAO();
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
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Đọc request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // Parse JSON
            JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
            
            if (jsonObject == null || !jsonObject.has("userId")) {
                sendErrorResponse(response, 400, "Missing required field: userId");
                return;
            }

            int userId = jsonObject.get("userId").getAsInt();
            String fullName = jsonObject.has("fullName") ? jsonObject.get("fullName").getAsString() : null;
            String phone = jsonObject.has("phone") ? jsonObject.get("phone").getAsString() : null;
            String avatarUrl = jsonObject.has("avatarUrl") ? jsonObject.get("avatarUrl").getAsString() : null;

            System.out.println("[DEBUG UpdateProfileController] Updating profile for userId=" + userId);

            // Cập nhật profile
            boolean success = profileDAO.updateProfile(userId, fullName, phone, avatarUrl);

            if (success) {
                sendSuccessResponse(response, "Profile updated successfully");
            } else {
                sendErrorResponse(response, 500, "Failed to update profile");
            }

        } catch (Exception e) {
            System.err.println("[ERROR UpdateProfileController] " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * Gửi response thành công
     */
    private void sendSuccessResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        String json = gson.toJson(new ApiResponse(true, message, null));
        response.getWriter().write(json);
    }

    /**
     * Gửi response lỗi
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        String json = gson.toJson(new ApiResponse(false, message, null));
        response.getWriter().write(json);
    }

    /**
     * Inner class cho API response
     */
    private static class ApiResponse {
        boolean success;
        String message;
        Object data;

        ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
    }

    /**
     * Set CORS headers
     */
    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
