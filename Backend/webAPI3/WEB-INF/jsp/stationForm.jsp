<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="DTO.Station" %>
<html>
<head><title>Station Form</title></head>
<body>
<h3>Station</h3>
<%
    Station s = (Station)request.getAttribute("station");
    String id = s==null?"":String.valueOf(s.getStation_ID());
    String name = s==null?"":s.getName();
    String address = s==null?"":s.getAddress();
%>
<form action="stations" method="post">
    <input type="hidden" name="action" value="save" />
    <input type="hidden" name="stationId" value="<%=id%>" />
    Name: <input type="text" name="name" value="<%=name%>" required /><br/>
    Address: <input type="text" name="address" value="<%=address%>" required /><br/>
    <button type="submit">Save</button>
    <a href="stations">Cancel</a>
</form>
</body>
</html>
