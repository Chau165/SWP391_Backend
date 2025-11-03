package DAO;

import DTO.DispatchRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mylib.DBUtils;

public class DispatchRequestDAO {
    public int createRequest(DispatchRequest req) {
    String sql = "INSERT INTO Dispatch_Log (Station_ID, Quantity, Status, Note) VALUES (?, ?, 'PENDING', ?)";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, req.getStationId());
            ps.setInt(2, req.getQuantity());
            ps.setString(3, req.getNote());
            int r = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    public List<DispatchRequest> getAllRequests() {
        List<DispatchRequest> list = new ArrayList<>();
    String sql = "SELECT ID, Station_ID, Quantity, Status, Note, Created_At, Processed_By, Assigned_From_Station_ID FROM Dispatch_Log ORDER BY Created_At DESC";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DispatchRequest d = new DispatchRequest();
                d.setId(rs.getInt("ID"));
                d.setStationId(rs.getInt("Station_ID"));
                d.setQuantity(rs.getInt("Quantity"));
                d.setStatus(rs.getString("Status"));
                d.setNote(rs.getString("Note"));
                d.setCreatedAt(rs.getTimestamp("Created_At"));
                d.setProcessedBy(rs.getObject("Processed_By") != null ? rs.getInt("Processed_By") : null);
                d.setAssignedFromStationId(rs.getObject("Assigned_From_Station_ID") != null ? rs.getInt("Assigned_From_Station_ID") : null);
                list.add(d);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public DispatchRequest getById(int id) {
    String sql = "SELECT ID, Station_ID, Quantity, Status, Note, Created_At, Processed_By, Assigned_From_Station_ID FROM Dispatch_Log WHERE ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DispatchRequest d = new DispatchRequest();
                    d.setId(rs.getInt("ID"));
                    d.setStationId(rs.getInt("Station_ID"));
                    d.setQuantity(rs.getInt("Quantity"));
                    d.setStatus(rs.getString("Status"));
                    d.setNote(rs.getString("Note"));
                    d.setCreatedAt(rs.getTimestamp("Created_At"));
                    d.setProcessedBy(rs.getObject("Processed_By") != null ? rs.getInt("Processed_By") : null);
                    d.setAssignedFromStationId(rs.getObject("Assigned_From_Station_ID") != null ? rs.getInt("Assigned_From_Station_ID") : null);
                    return d;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // Approve request: mark request approved, and reassign 'Quantity' batteries from fromStationId to the request station.
    // This method uses DB transaction to ensure atomicity.
    public boolean approveRequest(int requestId, int adminUserId) {
        Connection conn = null;
        try {
            conn = DBUtils.getConnection();
            conn.setAutoCommit(false);

            DispatchRequest req = getById(requestId);
            if (req == null) throw new RuntimeException("Request not found");
            if (!"PENDING".equalsIgnoreCase(req.getStatus())) throw new RuntimeException("Request not pending");

            // Find candidate stations that have available batteries (simple strategy: choose any station != req.stationId with available battery count >= quantity)
            String findSql = "SELECT TOP 1 st.Station_ID, COUNT(b.ID) AS avail FROM Station st JOIN Battery b ON st.Station_ID = b.Station_ID WHERE st.Station_ID <> ? AND b.Status = 'AVAILABLE' GROUP BY st.Station_ID HAVING COUNT(b.ID) >= ? ORDER BY avail DESC";
            try (PreparedStatement fps = conn.prepareStatement(findSql)) {
                fps.setInt(1, req.getStationId());
                fps.setInt(2, req.getQuantity());
                try (ResultSet frs = fps.executeQuery()) {
                    if (!frs.next()) {
                        conn.rollback();
                        return false; // no station can supply
                    }
                    int fromStationId = frs.getInt("Station_ID");

                    // Reassign 'Quantity' batteries from fromStationId to req.stationId
                    String selectB = "SELECT TOP (?) ID FROM Battery WHERE Station_ID = ? AND Status = 'AVAILABLE'";
                    try (PreparedStatement sp = conn.prepareStatement(selectB)) {
                        sp.setInt(1, req.getQuantity());
                        sp.setInt(2, fromStationId);
                        try (ResultSet brs = sp.executeQuery()) {
                            List<Integer> batteryIds = new ArrayList<>();
                            while (brs.next()) batteryIds.add(brs.getInt("ID"));

                            if (batteryIds.size() < req.getQuantity()) {
                                conn.rollback();
                                return false; // race condition
                            }

                            String upd = "UPDATE Battery SET Station_ID = ?, Status = 'ASSIGNED' WHERE ID = ?";
                            try (PreparedStatement up = conn.prepareStatement(upd)) {
                                for (int bid : batteryIds) {
                                    up.setInt(1, req.getStationId());
                                    up.setInt(2, bid);
                                    up.executeUpdate();
                                }
                            }

                            // mark request approved and record processed_by and assigned_from_station_id
                            String updReq = "UPDATE Dispatch_Log SET Status = 'APPROVED', Processed_By = ?, Assigned_From_Station_ID = ? WHERE ID = ?";
                            try (PreparedStatement ur = conn.prepareStatement(updReq)) {
                                ur.setInt(1, adminUserId);
                                ur.setInt(2, fromStationId);
                                ur.setInt(3, requestId);
                                ur.executeUpdate();
                            }

                            conn.commit();
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); if (conn != null) conn.close(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public boolean rejectRequest(int requestId, int adminUserId, String reason) {
    String sql = "UPDATE Dispatch_Log SET Status = 'REJECTED', Processed_By = ?, Note = ? WHERE ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminUserId);
            ps.setString(2, reason);
            ps.setInt(3, requestId);
            int r = ps.executeUpdate();
            return r > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}
