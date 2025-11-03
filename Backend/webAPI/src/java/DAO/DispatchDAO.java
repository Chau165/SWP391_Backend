package DAO;

import DTO.DispatchLog;
import mylib.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DispatchDAO {

    public int createDispatchRequest(DispatchLog d) throws ClassNotFoundException, SQLException {
        String sql = "INSERT INTO Dispatch_Log (Station_ID, Quantity, Status, Note, Created_At) OUTPUT INSERTED.ID VALUES (?,?,?,?,GETDATE())";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, d.getStation_ID());
            ps.setInt(2, d.getQuantity());
            ps.setString(3, d.getStatus());
            ps.setString(4, d.getNote());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<DispatchLog> getPendingDispatches() throws ClassNotFoundException, SQLException {
        String sql = "SELECT ID, Station_ID, Quantity, Status, Note, Created_At, Processed_By, Assigned_From_Station_ID FROM Dispatch_Log WHERE Status = 'Pending' ORDER BY Created_At ASC";
        List<DispatchLog> list = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DispatchLog d = new DispatchLog();
                d.setID(rs.getInt("ID"));
                d.setStation_ID(rs.getInt("Station_ID"));
                d.setQuantity(rs.getInt("Quantity"));
                d.setStatus(rs.getString("Status"));
                d.setNote(rs.getString("Note"));
                d.setCreated_At(rs.getTimestamp("Created_At"));
                int pb = rs.getInt("Processed_By");
                if (rs.wasNull()) pb = -1;
                d.setProcessed_By(pb == -1 ? null : pb);
                int af = rs.getInt("Assigned_From_Station_ID");
                if (rs.wasNull()) af = -1;
                d.setAssigned_From_Station_ID(af == -1 ? null : af);
                list.add(d);
            }
        }
        return list;
    }

    public boolean updateDispatchStatus(int id, String status, Integer processedBy, Integer assignedFromStationId) throws ClassNotFoundException, SQLException {
        String sql = "UPDATE Dispatch_Log SET Status = ?, Processed_By = ?, Assigned_From_Station_ID = ? WHERE ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (processedBy == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, processedBy);
            if (assignedFromStationId == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, assignedFromStationId);
            ps.setInt(4, id);
            int row = ps.executeUpdate();
            return row > 0;
        }
    }

    public boolean decrementStationBattery(int stationId, int quantity) throws ClassNotFoundException, SQLException {
        // The Station table no longer contains Total_Battery. This operation must be
        // re-implemented against the correct inventory source. For now, return false
        // to indicate the decrement did not occur.
        System.err.println("decrementStationBattery called but Station.Total_Battery column no longer exists");
        return false;
    }

    public int getStationBattery(int stationId) throws ClassNotFoundException, SQLException {
        // Total_Battery removed. Return -1 to indicate unknown/unavailable.
        System.err.println("getStationBattery called but Station.Total_Battery column no longer exists");
        return -1;
    }
}
