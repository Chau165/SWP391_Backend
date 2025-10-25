<%@ page import="java.sql.*,java.util.*,java.io.*" %>
<%@ page import="mylib.DBUtils" %>
<%
    // stations_with_charging.jsp - returns JSON array of stations with nested charging stations
    response.setContentType("application/json;charset=UTF-8");

    Connection conn = null;
    try {
        // Use central DBUtils from Source Packages for connection
        conn = DBUtils.getConnection();

        String ssql = "SELECT Station_ID, Name, Address FROM dbo.[Station] ORDER BY Station_ID";
        try (PreparedStatement ps = conn.prepareStatement(ssql); ResultSet rs = ps.executeQuery()) {
            out.print("[");
            boolean firstStation = true;
            while (rs.next()) {
                if (!firstStation) out.print(',');
                firstStation = false;
                int stationId = rs.getInt("Station_ID");
                String sname = rs.getString("Name");
                String saddr = rs.getString("Address");
                sname = sname==null?"":sname.replace("\\","\\\\").replace("\"","\\\"");
                saddr = saddr==null?"":saddr.replace("\\","\\\\").replace("\"","\\\"");

                out.print("{\"Station_ID\":"+stationId+",\"Name\":\""+sname+"\",\"Address\":\""+saddr+"\",\"ChargingStations\":");

                // query charging stations for this station
                String csql = "SELECT ChargingStation_ID, Station_ID, Name, Slot_Capacity, Slot_Type, Power_Rating FROM dbo.[Charging_Station] WHERE Station_ID=? ORDER BY ChargingStation_ID";
                try (PreparedStatement ps2 = conn.prepareStatement(csql)) {
                    ps2.setInt(1, stationId);
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        out.print("[");
                        boolean firstCs = true;
                        while (rs2.next()) {
                            if (!firstCs) out.print(',');
                            firstCs = false;
                            int csid = rs2.getInt("ChargingStation_ID");
                            int sid = rs2.getInt("Station_ID");
                            String csname = rs2.getString("Name");
                            int slotCap = rs2.getInt("Slot_Capacity");
                            String slotType = rs2.getString("Slot_Type");
                            String power = rs2.getString("Power_Rating");
                            csname = csname==null?"":csname.replace("\\","\\\\").replace("\"","\\\"");
                            slotType = slotType==null?"":slotType.replace("\\","\\\\").replace("\"","\\\"");
                            power = power==null?"":power.replace("\\","\\\\").replace("\"","\\\"");
                            out.print("{\"ChargingStation_ID\":"+csid+",\"Station_ID\":"+sid+",\"Name\":\""+csname+"\",\"Slot_Capacity\":"+slotCap+",\"Slot_Type\":\""+slotType+"\",\"Power_Rating\":\""+power+"\"}");
                        }
                        out.print("]");
                    }
                }

                out.print("}");
            }
            out.print("]");
        }

    } catch (Exception e) {
        response.setStatus(500);
        out.print("{\"error\":\"" + (e.getMessage()==null?e.toString():e.getMessage()).replace("\"","\\\"") + "\"}");
    } finally {
        if (conn!=null) try { conn.close(); } catch(Exception _){ }
    }
%>
