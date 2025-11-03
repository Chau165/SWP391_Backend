package DTO;

public class ChargingStation {
    private int ChargingStation_ID;
    private int Station_ID;
    private String Name;
    private int Slot_Capacity;
    private String Slot_Type;
    private double Power_Rating;

    public ChargingStation() {}

    public int getChargingStationId() { return ChargingStation_ID; }
    public void setChargingStationId(int id) { this.ChargingStation_ID = id; }

    public int getStationId() { return Station_ID; }
    public void setStationId(int stationId) { this.Station_ID = stationId; }

    public String getName() { return Name; }
    public void setName(String name) { this.Name = name; }

    public int getSlotCapacity() { return Slot_Capacity; }
    public void setSlotCapacity(int cap) { this.Slot_Capacity = cap; }

    public String getSlotType() { return Slot_Type; }
    public void setSlotType(String t) { this.Slot_Type = t; }

    public double getPowerRating() { return Power_Rating; }
    public void setPowerRating(double p) { this.Power_Rating = p; }
}
