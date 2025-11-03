package DAO;

import DTO.Comment;
import mylib.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentDAO {

    public int insertComment(Comment c) throws Exception {
        String sql = "INSERT INTO Comment(User_ID, Swap_ID, Content, Time_Post) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getUserId());
            if (c.getSwapId() != null) ps.setInt(2, c.getSwapId()); else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setString(3, c.getContent());
            ps.setTimestamp(4, new Timestamp(c.getTimePost().getTime()));
            return ps.executeUpdate();
        }
    }

    public List<Comment> getAllComments() throws Exception {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT Comment_ID, User_ID, Swap_ID, Content, Time_Post FROM Comment ORDER BY Time_Post DESC";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Comment c = new Comment();
                c.setCommentId(rs.getInt("Comment_ID"));
                c.setUserId(rs.getInt("User_ID"));
                int swapId = rs.getInt("Swap_ID");
                if (!rs.wasNull()) c.setSwapId(swapId); else c.setSwapId(null);
                c.setContent(rs.getString("Content"));
                Timestamp ts = rs.getTimestamp("Time_Post");
                if (ts != null) c.setTimePost(new Date(ts.getTime()));
                list.add(c);
            }
        }
        return list;
    }

    // For admin: get comments with full info (User Name, Station Name)
    public static class CommentDetailForAdmin {
        public int commentId;
        public int userId;
        public String userName;
        public Integer swapId;
        public Integer stationId;
        public String stationName;
        public String content;
        public Date timePost;
    }

    public List<CommentDetailForAdmin> getAllCommentsForAdmin() throws Exception {
        List<CommentDetailForAdmin> list = new ArrayList<>();
        String sql = "SELECT c.Comment_ID, c.User_ID, u.FullName AS UserName, c.Swap_ID, " +
                    "st.Station_ID, st.Name AS StationName, c.Content, c.Time_Post " +
                    "FROM Comment c " +
                    "INNER JOIN Users u ON c.User_ID = u.ID " +
                    "LEFT JOIN SwapTransaction swt ON c.Swap_ID = swt.ID " +
                    "LEFT JOIN Station st ON swt.Station_ID = st.Station_ID " +
                    "ORDER BY c.Time_Post DESC";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CommentDetailForAdmin c = new CommentDetailForAdmin();
                c.commentId = rs.getInt("Comment_ID");
                c.userId = rs.getInt("User_ID");
                c.userName = rs.getString("UserName");
                int swapId = rs.getInt("Swap_ID");
                c.swapId = rs.wasNull() ? null : swapId;
                int stationId = rs.getInt("Station_ID");
                c.stationId = rs.wasNull() ? null : stationId;
                c.stationName = rs.getString("StationName");
                c.content = rs.getString("Content");
                Timestamp ts = rs.getTimestamp("Time_Post");
                if (ts != null) c.timePost = new Date(ts.getTime());
                list.add(c);
            }
        }
        return list;
    }
}
