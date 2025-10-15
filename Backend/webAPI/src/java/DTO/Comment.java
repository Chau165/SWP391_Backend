package DTO;

import java.util.Date;

public class Comment {
    private int commentId;
    private int userId;
    private int swapId;  // Changed from stationId to swapId to match database schema
    private String content;
    private Date timePost;

    public Comment() {}

    public Comment(int commentId, int userId, int swapId, String content, Date timePost) {
        this.commentId = commentId;
        this.userId = userId;
        this.swapId = swapId;
        this.content = content;
        this.timePost = timePost;
    }

    public int getCommentId() { return commentId; }
    public void setCommentId(int commentId) { this.commentId = commentId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getSwapId() { return swapId; }
    public void setSwapId(int swapId) { this.swapId = swapId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getTimePost() { return timePost; }
    public void setTimePost(Date timePost) { this.timePost = timePost; }
}
