package DAO;

import DTO.PaymentTransaction;
import java.sql.*;
import mylib.DBUtils;

public class PaymentTransactionDAO {

    // DAO/PaymentTransactionDAO.java
    public int insertPayment(PaymentTransaction p) {
        String sql = "INSERT INTO PaymentTransaction "
                + "(User_ID, Station_ID, Package_ID, Amount, Payment_Method, Description, Transaction_Time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, p.getUser_ID());

            // Station_ID: NULL nếu không có
            if (p.getStation_ID() == null || p.getStation_ID() == 0) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, p.getStation_ID());
            }

            // Package_ID: NULL nếu không có (ví dụ đổi pin trả tiền theo lượt)
            if (p.getPackage_ID() == null || p.getPackage_ID() == 0) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, p.getPackage_ID());
            }

            ps.setDouble(4, p.getAmount());
            ps.setString(5, p.getPayment_Method());   // "VNPay" / "Cash" / ...
            ps.setString(6, p.getDescription());      // mô tả ngắn
            ps.setTimestamp(7, p.getTransaction_Time());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                return -1;
            }

            try ( ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public PaymentTransaction getPaymentById(int id) {
        PaymentTransaction payment = null;
        String sql = "SELECT * FROM PaymentTransaction WHERE ID = ?";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    payment = new PaymentTransaction();
                    payment.setID(rs.getInt("ID"));
                    payment.setUser_ID(rs.getInt("User_ID"));
                    payment.setStation_ID(rs.getInt("Station_ID"));
                    int pkg = rs.getInt("Package_ID");
                    payment.setPackage_ID(rs.wasNull() ? null : pkg);
                    payment.setAmount(rs.getDouble("Amount"));
                    payment.setPayment_Method(rs.getString("Payment_Method"));
                    payment.setDescription(rs.getString("Description"));
                    payment.setTransaction_Time(rs.getTimestamp("Transaction_Time"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return payment;
    }

    // ===== CẬP NHẬT TRẠNG THÁI / HOÀN TẤT THANH TOÁN =====
    public boolean updatePaymentStatus(int paymentId, String newStatus) {
        String sql = "UPDATE PaymentTransaction "
                + "SET Description = CONCAT(ISNULL(Description,''), ' - ', ?), "
                + "    Transaction_Time = ? "
                + "WHERE ID = ?";
        try ( Connection conn = DBUtils.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, paymentId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
