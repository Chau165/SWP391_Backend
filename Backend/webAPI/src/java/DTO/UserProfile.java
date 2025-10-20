package DTO;

import java.sql.Date;

/**
 * DTO cho User Profile - thông tin profile chi tiết của người dùng
 */
public class UserProfile {
    private int id;
    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private Integer currentPackageId;
    private String packageName;          // Tên gói pin
    private Date packageStartDate;
    private Date packageEndDate;
    private String avatarUrl;
    private java.sql.Timestamp createdAt;
    private java.sql.Timestamp updatedAt;

    public UserProfile() {
    }

    public UserProfile(int id, int userId, String fullName, String email, String phone, 
                      String role, Integer currentPackageId, String packageName,
                      Date packageStartDate, Date packageEndDate, String avatarUrl,
                      java.sql.Timestamp createdAt, java.sql.Timestamp updatedAt) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.currentPackageId = currentPackageId;
        this.packageName = packageName;
        this.packageStartDate = packageStartDate;
        this.packageEndDate = packageEndDate;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getCurrentPackageId() {
        return currentPackageId;
    }

    public void setCurrentPackageId(Integer currentPackageId) {
        this.currentPackageId = currentPackageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Date getPackageStartDate() {
        return packageStartDate;
    }

    public void setPackageStartDate(Date packageStartDate) {
        this.packageStartDate = packageStartDate;
    }

    public Date getPackageEndDate() {
        return packageEndDate;
    }

    public void setPackageEndDate(Date packageEndDate) {
        this.packageEndDate = packageEndDate;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public java.sql.Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.sql.Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
