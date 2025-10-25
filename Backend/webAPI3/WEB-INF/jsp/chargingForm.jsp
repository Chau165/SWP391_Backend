<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="DTO.ChargingStation" %>
<html>
<head><title>Charging Station Form</title></head>
<body>
<%
    ChargingStation c = (ChargingStation)request.getAttribute("charge");
    String id = c==null?"":String.valueOf(c.getChargingStationId());
    String name = c==null?"":c.getName();
    String cap = c==null?"":String.valueOf(c.getSlotCapacity());
    String type = c==null?"":c.getSlotType();
    String power = c==null?"":String.valueOf(c.getPowerRating());
    String stationId = request.getParameter("stationId");
%>
<form action="/webAPI3/charging" method="post">
    <input type="hidden" name="action" value="save" />
    <input type="hidden" name="chargingStationId" value="<%=id%>" />
    <input type="hidden" name="stationId" value="<%=stationId%>" />
    Name: <input type="text" name="name" value="<%=name%>" required/><br/>
    Slot Capacity: <input type="number" name="slotCapacity" value="<%=capacity%>" required/><br/>
    Slot Type: <select name="slotType"><option>Lithium-ion</option><option>LFP</option></select><br/>
    Power Rating: <input type="text" name="powerRating" value="<%=power%>" /><br/>
    <button type="submit">Save</button>
    <a href="/webAPI3/stations?action=detail&id=<%=stationId%>">Cancel</a>
</form>
</body>
</html>
