<%@ page import="java.util.List" %>
<%@ page import="DTO.Station" %>
<%@ page import="DTO.ChargingStation" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>Station Detail</title></head>
<body>
<%
    Station s = (Station)request.getAttribute("station");
    List<ChargingStation> slots = (List<ChargingStation>)request.getAttribute("slots");
%>
<h2>Station: <%= s!=null? s.getName():"" %></h2>
<p>Address: <%= s!=null? s.getAddress():"" %></p>
<a href="stations">Back to list</a>
<h3>Charging Stations</h3>
<a href="/webAPI3/charging?stationId=<%=s.getStation_ID()%>&action=new">Add Charging Station</a>
<table border="1">
    <tr><th>Name</th><th>Slot Capacity</th><th>Slot Type</th><th>Power Rating</th><th>Actions</th></tr>
    <%
        if (slots != null) {
            int totalSlots = 0; double totalPower = 0.0;
            for (ChargingStation cs: slots) {
                totalSlots += cs.getSlotCapacity();
                try { totalPower += cs.getPowerRating(); } catch (Exception e) {}
    %>
    <tr>
    <td><%=cs.getName()%></td>
    <td><%=cs.getSlotCapacity()%></td>
    <td><%=cs.getSlotType()%></td>
    <td><%=cs.getPowerRating()%></td>
        <td>
            <a href="/webAPI3/charging?action=edit&id=<%=cs.getChargingStationId()%>&stationId=<%=s.getStation_ID()%>">Edit</a> |
            <form style="display:inline" method="post" action="/webAPI3/charging" onsubmit="return confirm('Delete?')">
                <input type="hidden" name="action" value="delete" />
                <input type="hidden" name="id" value="<%=cs.getChargingStationId()%>" />
                <input type="hidden" name="stationId" value="<%=s.getStation_ID()%>" />
                <button type="submit">Delete</button>
            </form>
        </td>
    </tr>
    <%      }
            // summary box
    %>
    <tr><td colspan="5">Total slots: <%=slots!=null? slots.stream().mapToInt(ChargingStation::getSlotCapacity).sum():0%> | Estimated total power: <%=totalPower%> kW</td></tr>
    <%    }
    %>
</table>
</body>
</html>
