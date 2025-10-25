<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  DTO.Station station = (DTO.Station) request.getAttribute("station");
    boolean edit = station != null;
%>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title><%= edit ? "Edit Station" : "Create Station" %></title>
  <style>body{font-family:Arial} label{display:block;margin-top:8px}</style>
</head>
<body>
  <h1><%= edit ? "Edit Station" : "Create Station" %></h1>
  <form method="post" action="stations">
    <input type="hidden" name="action" value="<%= edit?"update":"create" %>" />
    <% if (edit) { %>
      <input type="hidden" name="stationId" value="<%= station.getStationId() %>" />
    <% } %>
    <label>Name</label>
    <input type="text" name="name" value="<%= edit?station.getName():"" %>" required />
    <label>Address</label>
    <input type="text" name="address" value="<%= edit?station.getAddress():"" %>" />
    <div style="margin-top:12px">
      <button type="submit">Save</button>
      <a href="stations">Cancel</a>
    </div>
  </form>
</body>
</html>
