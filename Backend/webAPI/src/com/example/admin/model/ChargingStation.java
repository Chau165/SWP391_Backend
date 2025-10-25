package com.example.admin.model;

public class ChargingStation {
    private int chargingStationId;
    private int stationId;
    private String name;
    private int slotCapacity;
    private String slotType;
    private double powerRating;

    public ChargingStation() {}

    // getters and setters
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

    public double getPowerRating() { return powerRating; }
    public void setPowerRating(double powerRating) { this.powerRating = powerRating; }
}
