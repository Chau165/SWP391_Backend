<%@ page import="java.util.List" %>
<%@ page import="DAO.ChargingStationDAO" %>
<%@ page import="DTO.ChargingStation" %>
<%
    // JSON API to return charging stations (slots) for a given stationId
    response.setContentType("application/json;charset=UTF-8");
    String stationIdParam = request.getParameter("stationId");
    try {
        int stationId = stationIdParam != null ? Integer.parseInt(stationIdParam) : 0;
        ChargingStationDAO dao = new ChargingStationDAO();
        List<ChargingStation> list = dao.getByStationId(stationId);
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (ChargingStation cs : list) {
            if (!first) sb.append(',');
            first = false;
            String name = cs.getName() == null ? "" : cs.getName().replace("\\", "\\\\").replace("\"", "\\\"");
            String slotType = cs.getSlotType() == null ? "" : cs.getSlotType().replace("\\", "\\\\").replace("\"", "\\\"");
            String power = String.valueOf(cs.getPowerRating());
            sb.append('{')
              .append("\"ChargingStation_ID\":").append(cs.getChargingStationId())
              .append(",\"Station_ID\":").append(cs.getStationId())
              .append(",\"Name\":\"").append(name).append('\"')
              .append(",\"Slot_Capacity\":").append(cs.getSlotCapacity())
              .append(",\"Slot_Type\":\"").append(slotType).append('\"')
              .append(",\"Power_Rating\":\"").append(power).append('\"')
              .append('}');
        }
        sb.append(']');
        out.print(sb.toString());
    } catch (Exception e) {
        response.setStatus(500);
        String msg = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "";
        out.print("{\"error\":\"" + msg + "\"}");
    }
%>
