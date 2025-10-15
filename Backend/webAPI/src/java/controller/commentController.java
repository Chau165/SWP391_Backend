package controller;

import DAO.CommentDAO;
import DTO.Comment;
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
import java.util.Date;
import java.util.List;
import DAO.SwapTransactionDAO;

@WebServlet(name = "commentController", urlPatterns = {"/api/comment"})
public class commentController extends HttpServlet {

    private final CommentDAO commentDAO = new CommentDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // POST: create a comment. Requires session User (Driver only)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = response.getWriter(); BufferedReader reader = request.getReader()) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"status\":\"fail\",\"message\":\"Not logged in\"}");
                return;
            }
            Users u = (Users) session.getAttribute("User");
            if (u == null) {
                System.out.println("DEBUG commentController - No User in session");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"status\":\"fail\",\"message\":\"Not logged in\"}");
                return;
            }
            System.out.println("DEBUG commentController - User logged in: ID=" + u.getId() + ", Role=" + u.getRole());

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            CreateCommentRequest req = gson.fromJson(sb.toString(), CreateCommentRequest.class);
            if (req == null || req.swapId == null || req.content == null || req.content.trim().isEmpty()) {
                System.out.println("DEBUG commentController - Missing data in request");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"fail\",\"message\":\"Missing swapId or content\"}");
                return;
            }

            // Only Drivers can comment. Staff cannot.
            if (u.getRole() == null || !u.getRole().equalsIgnoreCase("driver")) {
                System.out.println("DEBUG commentController - User is not Driver, role: " + u.getRole());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"status\":\"fail\",\"message\":\"Chỉ tài xế (Driver) mới được gửi nhận xét.\"}");
                return;
            }

            // Enforce that the driver has at least one COMPLETED swap transaction before allowing comment
            System.out.println("DEBUG commentController - Checking if user has completed swaps...");
            boolean hasSwaps = SwapTransactionDAO.userHasSwapTransactions(u.getId(), u.getRole());
            System.out.println("DEBUG commentController - Has completed swaps: " + hasSwaps);
            if (!hasSwaps) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"status\":\"fail\",\"message\":\"Bạn chưa có giao dịch hoàn thành (Status='Completed') nên không thể gửi nhận xét.\"}");
                return;
            }

            Comment c = new Comment();
            c.setUserId(u.getId());
            c.setSwapId(req.swapId);  // Changed from setStationId to setSwapId
            c.setContent(req.content.trim());
            c.setTimePost(new Date());

            int inserted = commentDAO.insertComment(c);
            if (inserted > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"status\":\"success\",\"message\":\"Comment saved\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"fail\",\"message\":\"Unable to save\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"Server error: " + e.getMessage().replace('"', '\'') + "\"}");
            }
        }
    }

    // GET: return comments only to admin role. Admin will see all comments.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        try (PrintWriter out = response.getWriter()) {
            if (session == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"status\":\"fail\",\"message\":\"Not logged in\"}");
                return;
            }
            Users u = (Users) session.getAttribute("User");
            if (u == null || !"admin".equalsIgnoreCase(u.getRole())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"status\":\"fail\",\"message\":\"Access denied\"}");
                return;
            }

            List<Comment> comments = commentDAO.getAllComments();
            out.print(gson.toJson(comments));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"Server error: " + e.getMessage().replace('"', '\'') + "\"}");
            }
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static class CreateCommentRequest {
        Integer swapId;  // Changed from stationId to swapId
        String content;
    }
}
