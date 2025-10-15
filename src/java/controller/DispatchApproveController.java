package controller;

import DAO.DispatchLogDAO;
import DTO.DispatchLog;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/api/secure/dispatchApprove")
public class DispatchApproveController extends HttpServlet {
    private final DispatchLogDAO dispatchLogDAO = new DispatchLogDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String role = (String) req.getAttribute("jwt_role");

        if (!"Admin".equals(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"success\":false,\"message\":\"Access denied\"}");
            return;
        }

        int requestId = Integer.parseInt(req.getParameter("requestId"));

        DispatchLog log = dispatchLogDAO.getById(requestId);
        if (log == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"success\":false,\"message\":\"Request not found\"}");
            return;
        }

        log.setRespond_Time(LocalDate.now());
        log.setStatus("Approved");
        dispatchLogDAO.update(log);

        resp.getWriter().write("{\"success\":true}");
    }
}
