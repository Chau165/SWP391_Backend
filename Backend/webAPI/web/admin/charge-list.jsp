<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    java.util.List charges = (java.util.List) request.getAttribute("charges");
    Integer stationId = (Integer) request.getAttribute("stationId");
    String message = (String) session.getAttribute("message"); if (message != null) session.removeAttribute("message");
%>
<!doctype html>
<html>
<head><meta charset="utf-8"><title>Charging Slots</title></head>
<body>
  <h1>Charging Slots for Station <%= stationId %></h1>
  <% if (message != null) { %><div style="background:#e8f5e9;padding:8px"><%= message %></div><% } %>
  <a href="charge-form.jsp?stationId=<%= stationId %>">Add Slot</a>
  <a href="stations">Back to Stations</a>
  <table style="width:100%;border-collapse:collapse;margin-top:8px"><thead><tr><th>ID</th><th>Name</th><th>Capacity</th><th>Type</th><th>Power</th><th>Actions</th></tr></thead><tbody>
  <% if (charges != null) {
     for (Object o : charges) {
       DTO.ChargingStation c = (DTO.ChargingStation)o;
  %>
    <tr>
      <td><%= c.getChargingStationId() %></td>
      <td><%= c.getName() %></td>
      <td><%= c.getSlotCapacity() %></td>
      <td><%= c.getSlotType() %></td>
      <td><%= c.getPowerRating() %></td>
      <td>
        <a href="charge-form.jsp?action=edit&id=<%= c.getChargingStationId() %>&stationId=<%= stationId %>">Edit</a>
        <form method="post" action="charging" style="display:inline" onsubmit="return confirm('Delete?')">
          <input type="hidden" name="action" value="delete" />
          <input type="hidden" name="id" value="<%= c.getChargingStationId() %>" />
          <input type="hidden" name="stationId" value="<%= stationId %>" />
          <button type="submit">Delete</button>
        </form>
      </td>
    </tr>
  <%   }
     }
  %>
  </tbody></table>
</body>
</html>
