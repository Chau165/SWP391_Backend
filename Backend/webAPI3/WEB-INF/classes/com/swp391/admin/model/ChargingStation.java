package com.swp391.admin.model;

// Model class for Charging_Station
public class ChargingStation {
    private int chargingStationId;
    private int stationId;
    private String name;
    private int slotCapacity;
    private String slotType;
    private String powerRating;

    public ChargingStation() {}

    public ChargingStation(int chargingStationId, int stationId, String name, int slotCapacity, String slotType, String powerRating) {
        this.chargingStationId = chargingStationId;
        this.stationId = stationId;
        this.name = name;
        this.slotCapacity = slotCapacity;
        this.slotType = slotType;
        this.powerRating = powerRating;
    }

    public int getChargingStationId() { return chargingStationId; }
    public void setChargingStationId(int chargingStationId) { this.chargingStationId = chargingStationId; }

    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSlotCapacity() { return slotCapacity; }
    public void setSlotCapacity(int slotCapacity) { this.slotCapacity = slotCapacity; }

    public String getSlotType() { return slotType; }
    public void setSlotType(String slotType) { this.slotType = slotType; }

    public String getPowerRating() { return powerRating; }
    public void setPowerRating(String powerRating) { this.powerRating = powerRating; }
}
