<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Serve the local admin UI inside this webapp. The compiled admin UI index is at ./index.html
    // Redirecting to local index.html so "Open Admin Dashboard" opens the admin UI under /TestWebAPI/admin/
    String target = "./index.html";
    try {
        response.sendRedirect(target);
    } catch (Exception e) {
        out.println("<p>Unable to open Admin Dashboard. Please open <a href='"+target+"'>"+target+"</a></p>");
    }
%>
