package controller;

import DTO.Users;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import mylib.DBUtils;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "TestSwapsController", urlPatterns = {"/api/testSwaps"})
public class TestSwapsController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            // Get user from session
            Users u = (Users) request.getSession().getAttribute("User");
            
            if (u == null) {
                out.print(gson.toJson(new TestResult("ERROR", "No user in session", null)));
                return;
            }

            int userId = u.getId();
            String role = u.getRole();
            
            System.out.println("===== TEST SWAPS CONTROLLER =====");
            System.out.println("User ID: " + userId);
            System.out.println("Role: " + role);

            // Test query directly
            List<SwapInfo> swaps = new ArrayList<>();
            String sql = "SELECT ID, Driver_ID, Status, Station_ID FROM SwapTransaction WHERE Driver_ID = ?";
            
            try (Connection conn = DBUtils.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    SwapInfo info = new SwapInfo();
                    info.id = rs.getInt("ID");
                    info.driverId = rs.getInt("Driver_ID");
                    info.status = rs.getString("Status");
                    info.stationId = rs.getInt("Station_ID");
                    info.statusLength = info.status != null ? info.status.length() : 0;
                    info.statusBytes = info.status != null ? info.status.getBytes().length : 0;
                    info.statusTrimmed = info.status != null ? info.status.trim() : null;
                    info.statusUpper = info.status != null ? info.status.trim().toUpperCase() : null;
                    info.matchesCompleted = "COMPLETED".equals(info.statusUpper);
                    swaps.add(info);
                    
                    System.out.println("  Swap ID: " + info.id);
                    System.out.println("    Status: [" + info.status + "]");
                    System.out.println("    Length: " + info.statusLength);
                    System.out.println("    Trimmed: [" + info.statusTrimmed + "]");
                    System.out.println("    Upper: [" + info.statusUpper + "]");
                    System.out.println("    Matches: " + info.matchesCompleted);
                }
                
                TestResult result = new TestResult(
                    "SUCCESS",
                    "Found " + swaps.size() + " swaps for user " + userId,
                    swaps
                );
                out.print(gson.toJson(result));
                System.out.println("===== END TEST =====");
                
            } catch (Exception e) {
                System.err.println("ERROR: " + e.getMessage());
                e.printStackTrace();
                out.print(gson.toJson(new TestResult("ERROR", e.getMessage(), null)));
            }
        }
    }

    static class TestResult {
        String status;
        String message;
        List<SwapInfo> swaps;
        
        TestResult(String status, String message, List<SwapInfo> swaps) {
            this.status = status;
            this.message = message;
            this.swaps = swaps;
        }
    }

    static class SwapInfo {
        int id;
        int driverId;
        String status;
        int stationId;
        int statusLength;
        int statusBytes;
        String statusTrimmed;
        String statusUpper;
        boolean matchesCompleted;
    }
}
