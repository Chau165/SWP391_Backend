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
        // Changed to use Swap_ID instead of Station_ID to match database schema
        String sql = "INSERT INTO Comment(User_ID, Swap_ID, Content, Time_Post) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getUserId());
            ps.setInt(2, c.getSwapId());  // Changed from getStationId to getSwapId
            ps.setString(3, c.getContent());
            ps.setTimestamp(4, new Timestamp(c.getTimePost().getTime()));
            System.out.println("DEBUG insertComment - userId=" + c.getUserId() + ", swapId=" + c.getSwapId());
            return ps.executeUpdate();
        }
    }

    public List<Comment> getAllComments() throws Exception {
        List<Comment> list = new ArrayList<>();
        // Changed to use Swap_ID instead of Station_ID
        String sql = "SELECT Comment_ID, User_ID, Swap_ID, Content, Time_Post FROM Comment ORDER BY Time_Post DESC";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Comment c = new Comment();
                c.setCommentId(rs.getInt("Comment_ID"));
                c.setUserId(rs.getInt("User_ID"));
                c.setSwapId(rs.getInt("Swap_ID"));  // Changed from setStationId to setSwapId
                c.setContent(rs.getString("Content"));
                Timestamp ts = rs.getTimestamp("Time_Post");
                if (ts != null) c.setTimePost(new Date(ts.getTime()));
                list.add(c);
            }
        }
        return list;
    }
}
