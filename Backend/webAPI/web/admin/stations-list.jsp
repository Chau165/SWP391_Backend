<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    java.util.List stations = (java.util.List) request.getAttribute("stations");
    String message = (String) session.getAttribute("message");
    if (message != null) { session.removeAttribute("message"); }
%>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Stations List</title>
  <style>body{font-family:Arial} table{border-collapse:collapse;width:100%} th,td{padding:8px;border:1px solid #ddd}</style>
</head>
<body>
  <h1>Stations</h1>
  <% if (message != null) { %>
    <div style="padding:8px;background:#e8f5e9;border:1px solid #c8e6c9;margin-bottom:10px"><%= message %></div>
  <% } %>

  <form method="get" action="stations">
    <input type="text" name="q" placeholder="Search name or address" value="<%= request.getParameter("q")!=null?request.getParameter("q") : "" %>" />
    <button type="submit">Search</button>
    <a href="stations?action=create">Create New Station</a>
  </form>

  <table>
    <thead><tr><th>ID</th><th>Name</th><th>Address</th><th>Actions</th></tr></thead>
    <tbody>
    <% if (stations != null) {
     for (Object o : stations) {
       DTO.Station s = (DTO.Station)o;
    %>
      <tr>
        <td><%= s.getStationId() %></td>
        <td><%= s.getName() %></td>
        <td><%= s.getAddress() %></td>
        <td>
          <a href="stations?action=edit&id=<%= s.getStationId() %>">Edit</a>
          <form method="post" action="stations" style="display:inline" onsubmit="return confirm('Delete?')">
            <input type="hidden" name="action" value="delete" />
            <input type="hidden" name="id" value="<%= s.getStationId() %>" />
            <button type="submit">Delete</button>
          </form>
          <a href="../charging?stationId=<%= s.getStationId() %>">View Slots</a>
        </td>
      </tr>
    <%   }
       }
    %>
    </tbody>
  </table>

</body>
</html>
