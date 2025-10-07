/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

/**
 *
 * @author Surface
 */
public class Vehicle {
    public int Vehicle_ID;
    public int User_ID;
    public String Model;
    public String Vin;
    public String License_Plate;
    public String Battery_Type_Current;

    public Vehicle() {
    }

    public Vehicle(int Vehicle_ID, int User_ID, String Model, String Vin, String License_Plate, String Battery_Type_Current) {
        this.Vehicle_ID = Vehicle_ID;
        this.User_ID = User_ID;
        this.Model = Model;
        this.Vin = Vin;
        this.License_Plate = License_Plate;
        this.Battery_Type_Current = Battery_Type_Current;
    }

    public int getVehicle_ID() {
        return Vehicle_ID;
    }

    public void setVehicle_ID(int Vehicle_ID) {
        this.Vehicle_ID = Vehicle_ID;
    }

    public int getUser_ID() {
        return User_ID;
    }

    public void setUser_ID(int User_ID) {
        this.User_ID = User_ID;
    }

    public String getModel() {
        return Model;
    }

    public void setModel(String Model) {
        this.Model = Model;
    }

    public String getVin() {
        return Vin;
    }

    public void setVin(String Vin) {
        this.Vin = Vin;
    }

    public String getLicense_Plate() {
        return License_Plate;
    }

    public void setLicense_Plate(String License_Plate) {
        this.License_Plate = License_Plate;
    }

    public String getBattery_Type_Current() {
        return Battery_Type_Current;
    }

    public void setBattery_Type_Current(String Battery_Type_Current) {
        this.Battery_Type_Current = Battery_Type_Current;
    }
    
    
}
