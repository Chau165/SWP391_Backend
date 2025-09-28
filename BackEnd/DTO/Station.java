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
    public int Total_Battery;

    public Station() {
    }

    public Station(int Station_ID, String Name, String Address, int Total_Battery) {
        this.Station_ID = Station_ID;
        this.Name = Name;
        this.Address = Address;
        this.Total_Battery = Total_Battery;
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
        return Total_Battery;
    }

    public void setTotal_Battery(int Total_Battery) {
        this.Total_Battery = Total_Battery;
    }
    
    
}
