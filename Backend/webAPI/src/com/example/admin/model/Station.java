package com.example.admin.model;

public class Station {
    private int stationId;
    private String name;
    private String address;

    public Station() {}

    public Station(int stationId, String name, String address) {
        this.stationId = stationId;
        this.name = name;
        this.address = address;
    }

    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
