package controller;

import DAO.UserProfileDAO;
import DTO.UserProfile;
import com.google.gson.Gson;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * API Controller để lấy thông tin profile của user
 * Endpoint: /api/profile
 * Method: GET
 * Params: userId (required)
 */
@WebServlet(name = "GetProfileController", urlPatterns = {"/api/profile"})
public class GetProfileController extends HttpServlet {

    private final UserProfileDAO profileDAO = new UserProfileDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Lấy userId từ parameter
            String userIdParam = request.getParameter("userId");
            
            if (userIdParam == null || userIdParam.trim().isEmpty()) {
                sendErrorResponse(response, 400, "Missing userId parameter");
                return;
            }

            int userId = Integer.parseInt(userIdParam);
            System.out.println("[DEBUG GetProfileController] Getting profile for userId=" + userId);

            // Lấy profile từ database
            UserProfile profile = profileDAO.getProfileByUserId(userId);

            if (profile != null) {
                // Trả về profile
                sendSuccessResponse(response, profile);
            } else {
                sendErrorResponse(response, 404, "Profile not found for userId=" + userId);
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, 400, "Invalid userId format");
        } catch (Exception e) {
            System.err.println("[ERROR GetProfileController] " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "Internal server error");
        }
    }

    /**
     * Gửi response thành công
     */
    private void sendSuccessResponse(HttpServletResponse response, UserProfile profile) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        String json = gson.toJson(new ApiResponse(true, "Profile retrieved successfully", profile));
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
