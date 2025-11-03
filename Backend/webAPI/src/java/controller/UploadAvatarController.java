package controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller để upload avatar
 * Nhận file từ client, lưu vào thư mục uploads, trả về URL
 */
@WebServlet(name = "UploadAvatarController", urlPatterns = {"/api/upload-avatar"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1,  // 1 MB
    maxFileSize = 1024 * 1024 * 10,       // 10 MB
    maxRequestSize = 1024 * 1024 * 15     // 15 MB
)
public class UploadAvatarController extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads/avatars";
    private static final long serialVersionUID = 1L;

    /**
     * CORS headers
     */
    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

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

        Gson gson = new Gson();
        Map<String, Object> result = new HashMap<>();

        try {
            // Get the file part
            Part filePart = request.getPart("avatar");
            
            if (filePart == null) {
                result.put("success", false);
                result.put("message", "No file uploaded");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // Get filename
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            
            // Validate file type
            String contentType = filePart.getContentType();
            if (!isValidImageType(contentType)) {
                result.put("success", false);
                result.put("message", "Invalid file type. Only images (jpg, png, gif) are allowed.");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // Generate unique filename
            String fileExtension = getFileExtension(fileName);
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Get application path (webapp folder)
            String applicationPath = request.getServletContext().getRealPath("");
            String uploadPath = applicationPath + File.separator + UPLOAD_DIR;

            // Create directory if not exists
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Save file
            String filePath = uploadPath + File.separator + uniqueFileName;
            filePart.write(filePath);

            // Generate URL for accessing the file
            String contextPath = request.getContextPath();
            String avatarUrl = contextPath + "/" + UPLOAD_DIR + "/" + uniqueFileName;

            System.out.println("[DEBUG UploadAvatarController] File uploaded successfully:");
            System.out.println("  - Original filename: " + fileName);
            System.out.println("  - Saved as: " + uniqueFileName);
            System.out.println("  - File path: " + filePath);
            System.out.println("  - Avatar URL: " + avatarUrl);

            result.put("success", true);
            result.put("message", "Avatar uploaded successfully");
            result.put("avatarUrl", avatarUrl);
            result.put("fileName", uniqueFileName);

        } catch (Exception e) {
            System.err.println("[ERROR UploadAvatarController] Upload failed: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "Upload failed: " + e.getMessage());
        }

        response.getWriter().write(gson.toJson(result));
    }

    /**
     * Validate image type
     */
    private boolean isValidImageType(String contentType) {
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif")
        );
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return ".jpg";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return ".jpg";
    }
}
