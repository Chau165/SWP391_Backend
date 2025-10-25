package DTO;

import java.util.Date;

public class DispatchRequest {
    private int id;
    private int stationId;
    private int quantity;
    private String status; // PENDING, APPROVED, REJECTED
    private String note;
    private Date createdAt;
    private Integer processedBy;
    private Integer assignedFromStationId;

    public DispatchRequest() {}

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Integer getProcessedBy() { return processedBy; }
    public void setProcessedBy(Integer processedBy) { this.processedBy = processedBy; }
    public Integer getAssignedFromStationId() { return assignedFromStationId; }
    public void setAssignedFromStationId(Integer assignedFromStationId) { this.assignedFromStationId = assignedFromStationId; }
}
