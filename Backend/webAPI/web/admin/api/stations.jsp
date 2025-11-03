<%@ page import="java.util.List" %>
<%@ page import="DAO.StationDAO" %>
<%@ page import="DTO.Station" %>
<%
    // Minimal JSON API: return array of stations
    response.setContentType("application/json;charset=UTF-8");
    String search = request.getParameter("search");
    try {
        StationDAO dao = new StationDAO();
        List<Station> list = (search == null || search.isEmpty()) ? dao.getAllStation() : dao.searchStation(search);
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (Station s : list) {
            if (!first) sb.append(',');
            first = false;
            String name = s.getName() == null ? "" : s.getName().replace("\\", "\\\\").replace("\"", "\\\"");
            String addr = s.getAddress() == null ? "" : s.getAddress().replace("\\", "\\\\").replace("\"", "\\\"");
            sb.append('{')
              .append("\"Station_ID\":").append(s.getStation_ID())
              .append(",\"Name\":\"").append(name).append('\"')
              .append(",\"Address\":\"").append(addr).append('\"')
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
