<%@ page import="java.util.List" %>
<%@ page import="DTO.Station" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Stations</title>
</head>
<body>
<h2>Station List</h2>
<a href="stations?action=new">Add Station</a>
<table border="1">
    <tr><th>Name</th><th>Address</th><th>Actions</th></tr>
    <%
        List<Station> stations = (List<Station>)request.getAttribute("stations");
        if (stations != null) {
            for (Station s: stations) {
    %>
    <tr>
        <td><a href="stations?action=detail&id=<%=s.getStationId()%>"><%=s.getName()%></a></td>
        <td><%=s.getAddress()%></td>
        <td>
            <a href="stations?action=edit&id=<%=s.getStationId()%>">Edit</a> |
            <a href="stations?action=delete&id=<%=s.getStationId()%>" onclick="return confirm('Delete?')">Delete</a>
        </td>
    </tr>
    <%      }
        }
    %>
</table>
</body>
</html>
