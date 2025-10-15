/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

import java.sql.Date;
import java.sql.Timestamp;

/**
 *
 * @author Surface
 */
public class SwapTransaction {

    public int ID;
    public int Driver_ID;
    public int Staff_ID;
    public int Station_ID;
    public int Old_Battery;
    public int New_Battery;
    public double SoH_Old;
    public double SoH_New;
    public double Fee;
    public int Payment_ID;
    public String Status;
    public Timestamp Swap_Time;
    public int Booking_ID;

    private int ChargingStation_ID;

    public SwapTransaction() {
    }

    public SwapTransaction(int ID, int Driver_ID, int Staff_ID, int Station_ID, int Old_Battery, int New_Battery, double SoH_Old, double SoH_New, double Fee, int Payment_ID, String Status, Timestamp Swap_Time, int Booking_ID) {
        this.ID = ID;
        this.Driver_ID = Driver_ID;
        this.Staff_ID = Staff_ID;
        this.Station_ID = Station_ID;
        this.Old_Battery = Old_Battery;
        this.New_Battery = New_Battery;
        this.SoH_Old = SoH_Old;
        this.SoH_New = SoH_New;
        this.Fee = Fee;
        this.Payment_ID = Payment_ID;
        this.Status = Status;
        this.Swap_Time = Swap_Time;
        this.Booking_ID = Booking_ID;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getDriver_ID() {
        return Driver_ID;
    }

    public void setDriver_ID(int Driver_ID) {
        this.Driver_ID = Driver_ID;
    }

    public int getStaff_ID() {
        return Staff_ID;
    }

    public void setStaff_ID(int Staff_ID) {
        this.Staff_ID = Staff_ID;
    }

    public int getStation_ID() {
        return Station_ID;
    }

    public void setStation_ID(int Station_ID) {
        this.Station_ID = Station_ID;
    }

    public int getOld_Battery() {
        return Old_Battery;
    }

    public void setOld_Battery(int Old_Battery) {
        this.Old_Battery = Old_Battery;
    }

    public int getNew_Battery() {
        return New_Battery;
    }

    public void setNew_Battery(int New_Battery) {
        this.New_Battery = New_Battery;
    }

    public double getSoH_Old() {
        return SoH_Old;
    }

    public void setSoH_Old(double SoH_Old) {
        this.SoH_Old = SoH_Old;
    }

    public double getSoH_New() {
        return SoH_New;
    }

    public void setSoH_New(double SoH_New) {
        this.SoH_New = SoH_New;
    }

    public double getFee() {
        return Fee;
    }

    public void setFee(double Fee) {
        this.Fee = Fee;
    }

    public int getPayment_ID() {
        return Payment_ID;
    }

    public void setPayment_ID(int Payment_ID) {
        this.Payment_ID = Payment_ID;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }

    public Timestamp getSwap_Time() {
        return Swap_Time;
    }

    public void setSwap_Time(Timestamp Swap_Time) {
        this.Swap_Time = Swap_Time;
    }

    public int getBooking_ID() {
        return Booking_ID;
    }

    public void setBooking_ID(int Booking_ID) {
        this.Booking_ID = Booking_ID;
    }

    public int getChargingStation_ID() {
        return ChargingStation_ID;
    }

    public void setChargingStation_ID(int id) {
        this.ChargingStation_ID = id;
    }
}
