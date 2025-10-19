package DAO;

import DTO.BatterySlot;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import mylib.DBUtils;

public class BatterySlotDAO {

    /**
     * Tìm 1 slot phù hợp và giữ chỗ ngay trong transaction.Điều kiện: -
     * s.ChargingStation_ID = stationId - s.State = 'Occupied',
     * s.Door_State='Closed', s.Condition='Good' - b.SoH trong [minSoH, maxSoH]
     * - (tuỳ chọn) t.Model = batteryModel
     *
     * @param con
     * @param stationId
     * @param batteryModel
     * @param minSoH
     * @param maxSoH
     * @return
     * @throws java.sql.SQLException
     */
    public BatterySlot findAndReserveSuitableSlot(Connection con,
            int stationId,
            String batteryModel,
            double minSoH,
            double maxSoH) throws SQLException {

        boolean hasModelFilter = batteryModel != null && !batteryModel.trim().isEmpty();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT TOP 1 ")
                .append("  s.Slot_ID, s.Slot_Code, s.Slot_Type, s.State, s.Door_State, ")
                .append("  s.Battery_ID, s.[Condition], s.Last_Update, s.ChargingStation_ID ")
                .append("FROM dbo.BatterySlot s WITH (UPDLOCK, ROWLOCK, READPAST) ")
                .append("JOIN dbo.Battery b ON s.Battery_ID = b.Battery_ID ")
                .append("JOIN dbo.Battery_Type t ON b.Type_ID = t.ID ")
                .append("JOIN dbo.Charging_Station cs ON s.ChargingStation_ID = cs.ChargingStation_ID ")
                .append("WHERE cs.Station_ID = ? ")
                .append("  AND s.State = 'Occupied' ")
                .append("  AND s.Door_State = 'Closed' ")
                .append("  AND s.[Condition] = 'Good' ")
                .append("  AND b.SoH BETWEEN ? AND ? ");
        if (hasModelFilter) {
            sb.append("  AND t.Model = ? ");
        }
        sb.append("ORDER BY b.SoH DESC, s.Last_Update ASC");

        BatterySlot picked = null;

        try ( PreparedStatement ps = con.prepareStatement(sb.toString())) {
            int i = 1;
            ps.setInt(i++, stationId);
            ps.setDouble(i++, minSoH);
            ps.setDouble(i++, maxSoH);
            if (hasModelFilter) {
                ps.setString(i++, batteryModel.trim());
            }

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    picked = new BatterySlot();
                    picked.setSlot_ID(rs.getInt("Slot_ID"));
                    picked.setSlot_Code(rs.getString("Slot_Code"));
                    picked.setSlot_Type(rs.getString("Slot_Type"));
                    picked.setState(rs.getString("State"));
                    picked.setDoor_State(rs.getString("Door_State"));
                    picked.setBattery_ID(rs.getInt("Battery_ID"));
                    picked.setCondition(rs.getString("Condition"));
                    picked.setLast_Update(rs.getTimestamp("Last_Update"));
                    picked.setChargingStation_ID(rs.getInt("ChargingStation_ID"));
                }
            }
        }

        if (picked == null) {
            return null;
        }

        String upd = "UPDATE dbo.BatterySlot "
                + "SET State='Reserved', Last_Update=SYSDATETIME() "
                + "WHERE Slot_ID=? AND State='Occupied'";
        try ( PreparedStatement up = con.prepareStatement(upd)) {
            up.setInt(1, picked.getSlot_ID());
            if (up.executeUpdate() == 0) {
                return null; // bị race
            }
        }

        picked.setState("Reserved");
        return picked;
    }

    public BatterySlot getSlotById(int slotId) {
        String sql = "SELECT Slot_ID, Slot_Code, Slot_Type, State, Door_State, Battery_ID, Condition, Last_Update, ChargingStation_ID "
                + "FROM dbo.BatterySlot WHERE Slot_ID = ?";
        try ( Connection con = DBUtils.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Gỡ pin khỏi slot (đặt Battery_ID = NULL, State = 'Empty', Condition = NULL)
    public boolean removeBatteryFromSlot(int slotId) {
        String sql = "UPDATE dbo.BatterySlot SET Battery_ID = NULL, State = 'Empty', Condition = NULL, Last_Update = ? WHERE Slot_ID = ?";
        try ( Connection con = DBUtils.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            ps.setInt(2, slotId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Đưa pin vào slot (đặt Battery_ID, State='Occupied', Condition=<Weak/Damage>)
    public boolean assignBatteryToSlot(int slotId, int batteryId, String condition) {
        String sql = "UPDATE dbo.BatterySlot SET Battery_ID = ?, State = 'Occupied', Condition = ?, Last_Update = ? WHERE Slot_ID = ?";
        try ( Connection con = DBUtils.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, batteryId);
            ps.setString(2, condition);
            ps.setTimestamp(3, Timestamp.from(Instant.now()));
            ps.setInt(4, slotId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy danh sách tình trạng các ô sạc trong tất cả trạm sạc thuộc 1 trạm cụ
     * thể mà nhân viên đang làm việc.
     *
     * @param stationId ID của trạm (Station_ID trong bảng Users)
     * @return danh sách slot kèm thông tin pin, SoH, kiểu trạm sạc
     */
    public List<BatterySlot> getSlotsByStationId(int stationId) {
        List<BatterySlot> list = new ArrayList<>();

        String sql = "SELECT s.Slot_ID, s.Slot_Code, s.Slot_Type, s.State, s.Door_State, "
                + "s.Battery_ID, s.[Condition], s.Last_Update, s.ChargingStation_ID, "
                + "b.SoH AS BatterySoH, b.Serial_Number AS BatterySerial, "
                + "cs.Name AS ChargingStationName, cs.Slot_Type AS ChargingSlotType "
                + "FROM dbo.BatterySlot s "
                + "JOIN dbo.Charging_Station cs ON s.ChargingStation_ID = cs.ChargingStation_ID "
                + "LEFT JOIN dbo.Battery b ON s.Battery_ID = b.Battery_ID "
                + "WHERE cs.Station_ID = ? "
                + "ORDER BY s.Slot_ID ASC";

        try ( Connection con = DBUtils.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, stationId);

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private BatterySlot mapRow(ResultSet rs) throws SQLException {
        BatterySlot slot = new BatterySlot();
        slot.setSlot_ID(rs.getInt("Slot_ID"));
        slot.setSlot_Code(rs.getString("Slot_Code"));
        slot.setSlot_Type(rs.getString("Slot_Type"));
        slot.setState(rs.getString("State"));
        slot.setDoor_State(rs.getString("Door_State"));
        slot.setBattery_ID((Integer) rs.getObject("Battery_ID"));
        slot.setCondition(rs.getString("Condition"));
        slot.setLast_Update(rs.getTimestamp("Last_Update"));
        slot.setChargingStation_ID(rs.getInt("ChargingStation_ID"));

        // ✅ Lấy thêm thông tin mở rộng
        slot.setBatterySoH(rs.getDouble("BatterySoH"));
        slot.setBatterySerial(rs.getString("BatterySerial"));
        slot.setChargingStationName(rs.getString("ChargingStationName"));
        slot.setChargingSlotType(rs.getString("ChargingSlotType"));

        return slot;
    }

}
