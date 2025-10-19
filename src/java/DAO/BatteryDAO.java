package DAO;

import DTO.Battery;
import mylib.DBUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BatteryDAO {

    // ✅ Hàm tiện ích thay cho String.isBlank() (tương thích Java 8)
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public Battery getBatteryById(int id) {
        String sql = "SELECT Battery_ID, Serial_Number, Resistance, SoH, Type_ID FROM dbo.Battery WHERE Battery_ID = ?";
        try ( Connection con = DBUtils.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
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

    public int insertBattery(Battery b) {
        String sql = "INSERT INTO dbo.Battery (Serial_Number, Resistance, SoH, Type_ID) VALUES (?, ?, ?, ?)";
        try ( Connection con = DBUtils.getConnection();  PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getSerialNumber());
            ps.setDouble(2, b.getResistance());
            ps.setDouble(3, b.getSoH());
            ps.setInt(4, b.getTypeId());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try ( ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean deleteBattery(int id) {
        String sql = "DELETE FROM dbo.Battery WHERE Battery_ID = ?";
        try ( Connection con = DBUtils.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Chuyển pin giữa hai trạm — theo hướng UPDATE-ONLY (không kiểm tra slot
     * trống) - Chỉ thay đổi quyền sở hữu (ChargingStation_ID) - Đặt Slot_Code =
     * NULL để đánh dấu chưa xếp ô
     *
     * @param stationFrom
     * @param stationTo
     * @param batteryTypeId
     * @param qtyGood
     * @param qtyAvg
     * @param qtyBad
     * @return
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public TransferResult transferBatteriesUpdateOnly(
            int stationFrom,
            int stationTo,
            int batteryTypeId,
            int qtyGood,
            int qtyAvg,
            int qtyBad
    ) throws SQLException, ClassNotFoundException {

        try ( Connection conn = DBUtils.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int targetCsId = getAnyChargingStation(conn, stationTo);
                if (targetCsId <= 0) {
                    return TransferResult.fail("No charging station found for target station ID=" + stationTo);
                }

                // Chuyển pin theo 3 nhóm SoH
                MoveResult good = moveByBand(conn, stationFrom, targetCsId, batteryTypeId, qtyGood, "b.SoH > 85");
                MoveResult avg = moveByBand(conn, stationFrom, targetCsId, batteryTypeId, qtyAvg, "b.SoH BETWEEN 50 AND 85");
                MoveResult bad = moveByBand(conn, stationFrom, targetCsId, batteryTypeId, qtyBad, "b.SoH < 50");

                conn.commit();

                TransferResult result = TransferResult.ok();
                result.movedGood = good.moved;
                result.movedAvg = avg.moved;
                result.movedBad = bad.moved;

                List<String> warnings = new ArrayList<>();
                if (good.moved < qtyGood) {
                    warnings.add("Good: requested " + qtyGood + " but moved " + good.moved);
                }
                if (avg.moved < qtyAvg) {
                    warnings.add("Average: requested " + qtyAvg + " but moved " + avg.moved);
                }
                if (bad.moved < qtyBad) {
                    warnings.add("Bad: requested " + qtyBad + " but moved " + bad.moved);
                }
                if (!warnings.isEmpty()) {
                    result.warning = String.join("; ", warnings);
                }

                return result;

            } catch (Exception ex) {
                conn.rollback();
                return TransferResult.fail("Transfer failed: " + ex.getMessage());
            }
        }
    }

    /**
     * Lấy bất kỳ 1 ChargingStation_ID thuộc trạm chỉ định
     */
    private int getAnyChargingStation(Connection conn, int stationId) throws SQLException {
        String sql = "SELECT TOP 1 ChargingStation_ID FROM Charging_Station WHERE Station_ID = ?";
        try ( PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Chuyển tối đa qty pin theo dải SoH. Không yêu cầu slot trống, chỉ cập
     * nhật ChargingStation_ID & Slot_Code=NULL
     */
    private MoveResult moveByBand(Connection conn,
            int stationFrom,
            int targetChargingStationId,
            int batteryTypeId,
            int qty,
            String sohCondition) throws SQLException {

        MoveResult r = new MoveResult();
        if (qty <= 0) {
            return r;
        }

        String pickSql
                = "SELECT TOP (?) b.Battery_ID "
                + "FROM Battery b "
                + "JOIN BatterySlot bs ON bs.Battery_ID = b.Battery_ID "
                + "JOIN Charging_Station cs ON cs.ChargingStation_ID = bs.ChargingStation_ID "
                + "WHERE cs.Station_ID = ? AND b.Type_ID = ? AND " + sohCondition
                + " ORDER BY b.Battery_ID";

        try ( PreparedStatement ps = conn.prepareStatement(pickSql)) {
            ps.setInt(1, qty);
            ps.setInt(2, stationFrom);
            ps.setInt(3, batteryTypeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int batteryId = rs.getInt("Battery_ID");
                String updateSql
                        = "UPDATE BatterySlot "
                        + "SET ChargingStation_ID = ?, "
                        + "    Slot_Code = NULL, "
                        + "    State = ?, " // 👈 thêm dòng này
                        + "    Last_Update = SYSDATETIME() "
                        + "WHERE Battery_ID = ? AND ChargingStation_ID <> ?"; // 👈 tránh double-move
                try ( PreparedStatement ups = conn.prepareStatement(updateSql)) {
                    ups.setInt(1, targetChargingStationId);
                    ups.setString(2, "IsAssign");           // 👈 state mới theo yêu cầu
                    ups.setInt(3, batteryId);
                    ups.setInt(4, targetChargingStationId); // 👈 tránh update nếu đã ở trạm đích
                    int affected = ups.executeUpdate();
                    if (affected > 0) {
                        r.moved++;
                    }
                }
            }
        }
        return r;
    }

    // --- Classes kết quả ---
    public static class TransferResult {

        public boolean success;
        public String message;
        public String warning;
        public int movedGood, movedAvg, movedBad;

        public static TransferResult ok() {
            TransferResult r = new TransferResult();
            r.success = true;
            return r;
        }

        public static TransferResult fail(String msg) {
            TransferResult r = new TransferResult();
            r.success = false;
            r.message = msg;
            return r;
        }
    }

    private static class MoveResult {

        int moved = 0;
    }

    private Battery mapRow(ResultSet rs) throws SQLException {
        Battery b = new Battery();
        b.setBatteryId(rs.getInt("Battery_ID"));
        b.setSerialNumber(rs.getString("Serial_Number"));
        b.setResistance(rs.getDouble("Resistance"));
        b.setSoH(rs.getDouble("SoH"));
        b.setTypeId(rs.getInt("Type_ID"));
        return b;
    }
}
