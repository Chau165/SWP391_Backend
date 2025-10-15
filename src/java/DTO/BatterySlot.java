package DTO;

import java.sql.Timestamp;

public class BatterySlot {
    private int slot_ID;
    private String slot_Code;
    private String slot_Type;
    private String state;
    private String door_State;
    private Integer battery_ID;   // có thể null
    private String condition;
    private Timestamp last_Update;
    private int chargingStation_ID;

    public int getSlot_ID() { return slot_ID; }
    public void setSlot_ID(int slot_ID) { this.slot_ID = slot_ID; }

    public String getSlot_Code() { return slot_Code; }
    public void setSlot_Code(String slot_Code) { this.slot_Code = slot_Code; }

    public String getSlot_Type() { return slot_Type; }
    public void setSlot_Type(String slot_Type) { this.slot_Type = slot_Type; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDoor_State() { return door_State; }
    public void setDoor_State(String door_State) { this.door_State = door_State; }

    public Integer getBattery_ID() { return battery_ID; }
    public void setBattery_ID(Integer battery_ID) { this.battery_ID = battery_ID; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public Timestamp getLast_Update() { return last_Update; }
    public void setLast_Update(Timestamp last_Update) { this.last_Update = last_Update; }

    public int getChargingStation_ID() { return chargingStation_ID; }
    public void setChargingStation_ID(int chargingStation_ID) { this.chargingStation_ID = chargingStation_ID; }
}
