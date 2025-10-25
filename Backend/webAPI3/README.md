# webAPI3 - Admin Dashboard (Station & Charging Station)

This folder contains a minimal Java Servlet + JSP backend (Tomcat) skeleton to manage Station and Charging_Station tables in SQL Server.

Key files added:
- `WEB-INF/web.xml` - servlet mappings.
- `WEB-INF/classes/com/swp391/admin/model/*` - Java model classes.
- `WEB-INF/classes/com/swp391/admin/dao/*` - DAO classes and DB connection.
- `WEB-INF/classes/com/swp391/admin/servlet/*` - Servlets for Station and ChargingStation.
- `WEB-INF/jsp/*` - JSP views (list, forms, detail).

Setup (NetBeans + Tomcat + MSSQL):
1. Create a new Web Application project in NetBeans or import these files into an existing webapp project. Make sure the web content root is `webAPI3`.
2. Add Microsoft JDBC driver (mssql-jdbc) to the project's libraries (or place the jar in Tomcat `lib`).
3. Edit `DBConnection.java` to set correct `URL`, `USER`, and `PASS` for your SQL Server instance.
   Example URL: `jdbc:sqlserver://localhost:1433;databaseName=BatterySwapDBVer2`
4. Build and deploy to Tomcat 9/10. Access the admin at: `http://localhost:8080/webAPI3/` and the stations list at `http://localhost:8080/webAPI3/stations`.

Notes and next steps:
- This is a minimal scaffold. You should move the Java sources into a proper `src` tree and compile with your IDE.
- Add validations, better error handling, authentication, and CSRF protections before using in production.
