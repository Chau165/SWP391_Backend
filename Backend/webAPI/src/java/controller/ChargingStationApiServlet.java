
package controller;

import DAO.ChargingStationDAO;
import DTO.ChargingStation;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "ChargingStationApiServlet", urlPatterns = {"/admin/api/charging_stations"})
public class ChargingStationApiServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String stationIdParam = req.getParameter("stationId");
        try (PrintWriter out = resp.getWriter()) {
            int stationId = stationIdParam != null ? Integer.parseInt(stationIdParam) : 0;
            ChargingStationDAO dao = new ChargingStationDAO();
            List<ChargingStation> list = dao.getByStationId(stationId);
            out.print(gson.toJson(list));
        } catch (Exception e) {
            resp.setStatus(500);
            try (PrintWriter out = resp.getWriter()) {
                out.print(gson.toJson(java.util.Collections.singletonMap("error", e.getMessage())));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            java.util.Map body = gson.fromJson(reader, java.util.Map.class);
            if (body == null || !body.containsKey("action")) { resp.setStatus(400); out.print(gson.toJson(java.util.Collections.singletonMap("error","action required"))); return; }
            String action = String.valueOf(body.get("action"));
            ChargingStationDAO dao = new ChargingStationDAO();
            if ("create".equalsIgnoreCase(action)) {
                ChargingStation cs = new ChargingStation();
                cs.setStationId(parseInt(body.getOrDefault("station_id", body.getOrDefault("Station_ID",0))));
                cs.setName(String.valueOf(body.getOrDefault("name", body.getOrDefault("Name", ""))));
                cs.setSlotCapacity(parseInt(body.getOrDefault("slot_capacity", body.getOrDefault("Slot_Capacity",0))));
                cs.setSlotType(String.valueOf(body.getOrDefault("slot_type", body.getOrDefault("Slot_Type", ""))));
                cs.setPowerRating(parseDouble(body.getOrDefault("power_rating", body.getOrDefault("Power_Rating",0))));
                System.out.println("[ChargingStationApiServlet] create body="+gson.toJson(body));
                boolean ok = dao.insert(cs);
                System.out.println("[ChargingStationApiServlet] insert returned: " + ok);
                out.print(gson.toJson(java.util.Collections.singletonMap("success", ok)));
                return;
            } else if ("update".equalsIgnoreCase(action)) {
                ChargingStation cs = new ChargingStation();
                cs.setChargingStationId(parseInt(body.getOrDefault("id", body.getOrDefault("ChargingStation_ID",0))));
                cs.setStationId(parseInt(body.getOrDefault("station_id", body.getOrDefault("Station_ID",0))));
                cs.setName(String.valueOf(body.getOrDefault("name", body.getOrDefault("Name", ""))));
                cs.setSlotCapacity(parseInt(body.getOrDefault("slot_capacity", body.getOrDefault("Slot_Capacity",0))));
                cs.setSlotType(String.valueOf(body.getOrDefault("slot_type", body.getOrDefault("Slot_Type", ""))));
                cs.setPowerRating(parseDouble(body.getOrDefault("power_rating", body.getOrDefault("Power_Rating",0))));
                boolean ok = dao.update(cs);
                out.print(gson.toJson(java.util.Collections.singletonMap("success", ok)));
                return;
            } else {
                resp.setStatus(400); out.print(gson.toJson(java.util.Collections.singletonMap("error","unknown action"))); return;
            }
        } catch (Exception ex) {
            resp.setStatus(500);
            resp.getWriter().print(gson.toJson(java.util.Collections.singletonMap("error", ex.getMessage())));
        }
    }

    private static int parseInt(Object o){
        if (o == null) return 0;
        if (o instanceof Number) return ((Number)o).intValue();
    try { return Integer.parseInt(String.valueOf(o)); } catch(Exception e){ try{ return (int)Double.parseDouble(String.valueOf(o)); }catch(Exception ex){ return 0; } }
    }

    private static double parseDouble(Object o){
        if (o == null) return 0.0;
        if (o instanceof Number) return ((Number)o).doubleValue();
    try { return Double.parseDouble(String.valueOf(o)); } catch(Exception e){ return 0.0; }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String idParam = req.getParameter("id");
        try (PrintWriter out = resp.getWriter()) {
            if (idParam == null) { resp.setStatus(400); out.print(gson.toJson(java.util.Collections.singletonMap("error","id required"))); return; }
            int id = Integer.parseInt(idParam);
            ChargingStationDAO dao = new ChargingStationDAO();
            boolean ok = dao.delete(id);
            out.print(gson.toJson(java.util.Collections.singletonMap("success", ok)));
        } catch (Exception ex) {
            resp.setStatus(500);
            resp.getWriter().print(gson.toJson(java.util.Collections.singletonMap("error", ex.getMessage())));
        }
    }
}
