package controller;

import DAO.PackageDAO;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/package/delete")
public class deletePackageController extends HttpServlet {

    private final PackageDAO packageDAO = new PackageDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = response.getWriter(); BufferedReader reader = request.getReader()) {

            // Đọc packageId từ request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // Parse JSON để lấy packageId
            DeleteRequest deleteRequest = gson.fromJson(sb.toString(), DeleteRequest.class);

            if (deleteRequest == null || deleteRequest.packageId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Invalid package ID\"}");
                return;
            }

            boolean isDeleted = packageDAO.deletePackage(deleteRequest.packageId);

            if (isDeleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"status\":\"success\",\"message\":\"Package deleted successfully\",\"packageId\":" + deleteRequest.packageId + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"status\":\"fail\",\"message\":\"Package not found or already deleted\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"Server error: "
                        + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // Alternative: DELETE with path parameter /api/package/delete/{id}
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = response.getWriter(); BufferedReader reader = request.getReader()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            DeleteRequest deleteRequest = gson.fromJson(sb.toString(), DeleteRequest.class);

            if (deleteRequest == null || deleteRequest.packageId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Invalid package ID\"}");
                return;
            }

            boolean isDeleted = packageDAO.deletePackage(deleteRequest.packageId);

            if (isDeleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"status\":\"success\",\"message\":\"Package deleted successfully\",\"packageId\":" + deleteRequest.packageId + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"status\":\"fail\",\"message\":\"Package not found or already deleted\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"Server error: "
                        + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static class DeleteRequest {
        int packageId;
    }
}
