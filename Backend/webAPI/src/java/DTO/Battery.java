package DTO;

public class Battery {
    private int id;
    private String serial;
    private String status;
    private Integer stationId;

    public Battery() {}

    public Battery(int id, String serial, String status, Integer stationId) {
        this.id = id;
        this.serial = serial;
        this.status = status;
        this.stationId = stationId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getStationId() { return stationId; }
    public void setStationId(Integer stationId) { this.stationId = stationId; }
}
