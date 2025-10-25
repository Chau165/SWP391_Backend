package DTO;

import java.util.Date;

public class DispatchLog {
    private int ID;
    private int Station_ID;
    private int Quantity;
    private String Status;
    private String Note;
    private Date Created_At;
    private Integer Processed_By;
    private Integer Assigned_From_Station_ID;

    public DispatchLog() {
    }

    public DispatchLog(int ID, int Station_ID, int Quantity, String Status, String Note, Date Created_At, Integer Processed_By, Integer Assigned_From_Station_ID) {
        this.ID = ID;
        this.Station_ID = Station_ID;
        this.Quantity = Quantity;
        this.Status = Status;
        this.Note = Note;
        this.Created_At = Created_At;
        this.Processed_By = Processed_By;
        this.Assigned_From_Station_ID = Assigned_From_Station_ID;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getStation_ID() {
        return Station_ID;
    }

    public void setStation_ID(int Station_ID) {
        this.Station_ID = Station_ID;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int Quantity) {
        this.Quantity = Quantity;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }

    public String getNote() {
        return Note;
    }

    public void setNote(String Note) {
        this.Note = Note;
    }

    public Date getCreated_At() {
        return Created_At;
    }

    public void setCreated_At(Date Created_At) {
        this.Created_At = Created_At;
    }

    public Integer getProcessed_By() {
        return Processed_By;
    }

    public void setProcessed_By(Integer Processed_By) {
        this.Processed_By = Processed_By;
    }

    public Integer getAssigned_From_Station_ID() {
        return Assigned_From_Station_ID;
    }

    public void setAssigned_From_Station_ID(Integer Assigned_From_Station_ID) {
        this.Assigned_From_Station_ID = Assigned_From_Station_ID;
    }
}
