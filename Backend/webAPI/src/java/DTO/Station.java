/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

/**
 *
 * @author Surface
 */
public class Station {
    public int Station_ID;
    public String Name;
    public String Address;

    public Station() {
    }

    public Station(int Station_ID, String Name, String Address) {
        this.Station_ID = Station_ID;
        this.Name = Name;
        this.Address = Address;
    }
    public int getStation_ID() {
        return Station_ID;
    }

    public void setStation_ID(int Station_ID) {
        this.Station_ID = Station_ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }

    public int getTotal_Battery() {
        // Total_Battery removed from schema; return 0 as placeholder
        return 0;
    }
    public void setTotal_Battery(int Total_Battery) {
        // no-op: Total_Battery field removed from Station table
    }
    
    
}
