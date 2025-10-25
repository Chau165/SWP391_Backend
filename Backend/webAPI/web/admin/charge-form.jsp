<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String action = request.getParameter("action");
  DTO.ChargingStation charge = (DTO.ChargingStation) request.getAttribute("charge");
    boolean edit = (charge != null) || ("edit".equals(action));
    String stationId = request.getParameter("stationId")!=null?request.getParameter("stationId"):(charge!=null?String.valueOf(charge.getStationId()):"");
%>
<!doctype html>
<html>
<head><meta charset="utf-8"><title><%= edit?"Edit Slot":"Create Slot" %></title></head>
<body>
  <h1><%= edit?"Edit Slot":"Create Slot" %> for Station <%= stationId %></h1>
  <form method="post" action="charging">
    <input type="hidden" name="action" value="<%= edit?"update":"create" %>" />
    <input type="hidden" name="stationId" value="<%= stationId %>" />
    <% if (edit && charge!=null) { %>
      <input type="hidden" name="chargingStationId" value="<%= charge.getChargingStationId() %>" />
    <% } %>
    <label>Name</label>
    <input type="text" name="name" value="<%= edit && charge!=null?charge.getName():"" %>" required />
    <label>Slot Capacity</label>
    <input type="number" name="slotCapacity" value="<%= edit && charge!=null?charge.getSlotCapacity():"" %>" />
    <label>Slot Type</label>
    <input type="text" name="slotType" value="<%= edit && charge!=null?charge.getSlotType():"" %>" />
    <label>Power Rating</label>
    <input type="text" name="powerRating" value="<%= edit && charge!=null?charge.getPowerRating():"" %>" />
    <div style="margin-top:8px"><button type="submit">Save</button> <a href="charge-list.jsp?stationId=<%= stationId %>">Cancel</a></div>
  </form>
</body>
</html>
