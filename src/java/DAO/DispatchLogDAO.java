package DAO;

import DTO.DispatchLog;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import mylib.DBUtils;

public class DispatchLogDAO {

    // Insert request mới
    public int insert(DispatchLog log) {
        String sql = "INSERT INTO Dispatch_Log (Station_Request_ID, Station_Respond_ID, BatteryType_Request_ID, "
                + "Quantity_Type_Good, Quantity_Type_Average, Quantity_Type_Bad, Request_Time, Respond_Time, Status) "
                + "OUTPUT INSERTED.ID VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, log.getStation_Request_ID());
            if (log.getStation_Respond_ID() != 0) {
                ps.setInt(2, log.getStation_Respond_ID());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }

            ps.setInt(3, log.getBatteryType_Request_ID());
            ps.setInt(4, log.getQuantity_Type_Good());
            ps.setInt(5, log.getQuantity_Type_Average());
            ps.setInt(6, log.getQuantity_Type_Bad());
            ps.setDate(7, Date.valueOf(log.getRequest_Time()));
            if (log.getRespond_Time() != null) {
                ps.setDate(8, Date.valueOf(log.getRespond_Time()));
            } else {
                ps.setNull(8, java.sql.Types.DATE);
            }

            ps.setString(9, log.getStatus());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Update request (Admin approve)
    public boolean update(DispatchLog log) {
        String sql = "UPDATE Dispatch_Log SET Station_Respond_ID=?, Respond_Time=?, Status=? WHERE ID=?";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, log.getStation_Respond_ID());
            ps.setDate(2, Date.valueOf(log.getRespond_Time()));
            ps.setString(3, log.getStatus());
            ps.setInt(4, log.getID());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy request theo ID
    public DispatchLog getById(int id) {
        String sql = "SELECT * FROM Dispatch_Log WHERE ID=?";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                DispatchLog log = new DispatchLog();
                log.setID(rs.getInt("ID"));
                log.setStation_Request_ID(rs.getInt("Station_Request_ID"));
                log.setStation_Respond_ID(rs.getInt("Station_Respond_ID"));
                log.setBatteryType_Request_ID(rs.getInt("BatteryType_Request_ID"));
                log.setQuantity_Type_Good(rs.getInt("Quantity_Type_Good"));
                log.setQuantity_Type_Average(rs.getInt("Quantity_Type_Average"));
                log.setQuantity_Type_Bad(rs.getInt("Quantity_Type_Bad"));

                Date req = rs.getDate("Request_Time");
                if (req != null) {
                    log.setRequest_Time(req.toLocalDate());
                }

                Date resp = rs.getDate("Respond_Time");
                if (resp != null) {
                    log.setRespond_Time(resp.toLocalDate());
                }

                log.setStatus(rs.getString("Status"));
                return log;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Lấy danh sách request Pending chung (Admin)
    public List<DispatchLog> getAllPendingRequests() {
        List<DispatchLog> list = new ArrayList<>();
        String sql = "SELECT * FROM Dispatch_Log WHERE Status='Pending'";
        try ( Connection conn = DBUtils.getConnection();  Statement st = conn.createStatement();  ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                DispatchLog log = new DispatchLog();
                log.setID(rs.getInt("ID"));
                log.setStation_Request_ID(rs.getInt("Station_Request_ID"));
                log.setStation_Respond_ID(rs.getInt("Station_Respond_ID"));
                log.setBatteryType_Request_ID(rs.getInt("BatteryType_Request_ID"));
                log.setQuantity_Type_Good(rs.getInt("Quantity_Type_Good"));
                log.setQuantity_Type_Average(rs.getInt("Quantity_Type_Average"));
                log.setQuantity_Type_Bad(rs.getInt("Quantity_Type_Bad"));

                if (rs.getDate("Request_Time") != null) {
                    log.setRequest_Time(rs.getDate("Request_Time").toLocalDate());
                }
                if (rs.getDate("Respond_Time") != null) {
                    log.setRespond_Time(rs.getDate("Respond_Time").toLocalDate());
                }

                log.setStatus(rs.getString("Status"));
                list.add(log);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy danh sách request Pending (Manager)
    public List<DispatchLog> getPendingRequests() {
        List<DispatchLog> list = new ArrayList<>();
        String sql = "SELECT * FROM Dispatch_Log WHERE Status='Pending'";
        try ( Connection conn = DBUtils.getConnection();  Statement st = conn.createStatement();  ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                DispatchLog log = new DispatchLog();
                log.setID(rs.getInt("ID"));
                log.setStation_Request_ID(rs.getInt("Station_Request_ID"));
                log.setStation_Respond_ID(rs.getInt("Station_Respond_ID"));
                log.setBatteryType_Request_ID(rs.getInt("BatteryType_Request_ID"));
                log.setQuantity_Type_Good(rs.getInt("Quantity_Type_Good"));
                log.setQuantity_Type_Average(rs.getInt("Quantity_Type_Average"));
                log.setQuantity_Type_Bad(rs.getInt("Quantity_Type_Bad"));

                if (rs.getDate("Request_Time") != null) {
                    log.setRequest_Time(rs.getDate("Request_Time").toLocalDate());
                }
                if (rs.getDate("Respond_Time") != null) {
                    log.setRespond_Time(rs.getDate("Respond_Time").toLocalDate());
                }

                log.setStatus(rs.getString("Status"));
                list.add(log);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy pending request cho trạm cụ thể
    public List<DispatchLog> getPendingRequestsByStation(int stationId) {
        List<DispatchLog> list = new ArrayList<>();
        String sql = "SELECT * FROM Dispatch_Log WHERE Status='Pending' AND Station_Respond_ID=?";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DispatchLog log = new DispatchLog();
                log.setID(rs.getInt("ID"));
                log.setStation_Request_ID(rs.getInt("Station_Request_ID"));
                log.setStation_Respond_ID(rs.getInt("Station_Respond_ID"));
                log.setBatteryType_Request_ID(rs.getInt("BatteryType_Request_ID"));
                log.setQuantity_Type_Good(rs.getInt("Quantity_Type_Good"));
                log.setQuantity_Type_Average(rs.getInt("Quantity_Type_Average"));
                log.setQuantity_Type_Bad(rs.getInt("Quantity_Type_Bad"));

                if (rs.getDate("Request_Time") != null) {
                    log.setRequest_Time(rs.getDate("Request_Time").toLocalDate());
                }
                if (rs.getDate("Respond_Time") != null) {
                    log.setRespond_Time(rs.getDate("Respond_Time").toLocalDate());
                }

                log.setStatus(rs.getString("Status"));
                list.add(log);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<DispatchLog> getRelatedRequestsByStation(int stationId) throws ClassNotFoundException {
        List<DispatchLog> list = new ArrayList<>();

        String sql
                = "SELECT ID, Station_Request_ID, Station_Respond_ID, BatteryType_Request_ID, "
                + "       Quantity_Type_Good, Quantity_Type_Average, Quantity_Type_Bad, "
                + "       Request_Time, Respond_Time, Status "
                + "FROM Dispatch_Log "
                + "WHERE Station_Request_ID = ? OR Station_Respond_ID = ? "
                + "ORDER BY COALESCE(Respond_Time, Request_Time) DESC";

        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stationId);
            ps.setInt(2, stationId);

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DispatchLog log = new DispatchLog();

                    log.setID(rs.getInt("ID"));
                    log.setStation_Request_ID(rs.getInt("Station_Request_ID"));

                    // Có thể null, nên kiểm tra trước khi set
                    int respondId = rs.getInt("Station_Respond_ID");
                    if (!rs.wasNull()) {
                        log.setStation_Respond_ID(respondId);
                    }

                    int batteryTypeId = rs.getInt("BatteryType_Request_ID");
                    if (!rs.wasNull()) {
                        log.setBatteryType_Request_ID(batteryTypeId);
                    }

                    log.setQuantity_Type_Good(rs.getInt("Quantity_Type_Good"));
                    log.setQuantity_Type_Average(rs.getInt("Quantity_Type_Average"));
                    log.setQuantity_Type_Bad(rs.getInt("Quantity_Type_Bad"));
                    Timestamp reqTime = rs.getTimestamp("Request_Time");
                    if (reqTime != null) {
                        log.setRequest_Time(reqTime.toLocalDateTime().toLocalDate()); // ✅ LocalDate
                    }

                    Timestamp resTime = rs.getTimestamp("Respond_Time");
                    if (resTime != null) {
                        log.setRespond_Time(resTime.toLocalDateTime().toLocalDate()); // ✅ LocalDate
                    }

                    log.setStatus(rs.getString("Status"));

                    list.add(log);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

}
