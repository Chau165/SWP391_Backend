package DTO;

public class ChargingStation {
    private int chargingStation_ID;
    private int station_ID;
    private String name;
    private int slot_Capacity;
    private String slot_Type;
    private double power_Rating;

    // Constructors
    public ChargingStation() {}

    public ChargingStation(int chargingStation_ID, int station_ID, String name, 
                          int slot_Capacity, String slot_Type, double power_Rating) {
        this.chargingStation_ID = chargingStation_ID;
        this.station_ID = station_ID;
        this.name = name;
        this.slot_Capacity = slot_Capacity;
        this.slot_Type = slot_Type;
        this.power_Rating = power_Rating;
    }

    // Getters and Setters
    public int getChargingStation_ID() {
        return chargingStation_ID;
    }

    public void setChargingStation_ID(int chargingStation_ID) {
        this.chargingStation_ID = chargingStation_ID;
    }

    public int getStation_ID() {
        return station_ID;
    }

    public void setStation_ID(int station_ID) {
        this.station_ID = station_ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSlot_Capacity() {
        return slot_Capacity;
    }

    public void setSlot_Capacity(int slot_Capacity) {
        this.slot_Capacity = slot_Capacity;
    }

    public String getSlot_Type() {
        return slot_Type;
    }

    public void setSlot_Type(String slot_Type) {
        this.slot_Type = slot_Type;
    }

    public double getPower_Rating() {
        return power_Rating;
    }

    public void setPower_Rating(double power_Rating) {
        this.power_Rating = power_Rating;
    }

    @Override
    public String toString() {
        return "ChargingStation{" +
                "chargingStation_ID=" + chargingStation_ID +
                ", station_ID=" + station_ID +
                ", name='" + name + '\'' +
                ", slot_Capacity=" + slot_Capacity +
                ", slot_Type='" + slot_Type + '\'' +
                ", power_Rating=" + power_Rating +
                '}';
    }
}