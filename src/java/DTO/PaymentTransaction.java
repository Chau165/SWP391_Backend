/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

import java.sql.Timestamp;

/**
 *
 * @author Surface
 */
public class PaymentTransaction {
    public int ID;
    public int User_ID;
    public int Station_ID;
    public Integer Package_ID;
    public double Amount;
    public String Payment_Method;
    public String Description;
    public Timestamp Transaction_Time;

    public PaymentTransaction() {
    }

    public PaymentTransaction(int ID, int User_ID, int Station_ID, Integer Package_ID, double Amount, String Payment_Method, String Description, Timestamp Transaction_Time) {
        this.ID = ID;
        this.User_ID = User_ID;
        this.Station_ID = Station_ID;
        this.Package_ID = Package_ID;
        this.Amount = Amount;
        this.Payment_Method = Payment_Method;
        this.Description = Description;
        this.Transaction_Time = Transaction_Time;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getUser_ID() {
        return User_ID;
    }

    public void setUser_ID(int User_ID) {
        this.User_ID = User_ID;
    }

    public int getStation_ID() {
        return Station_ID;
    }

    public void setStation_ID(int Station_ID) {
        this.Station_ID = Station_ID;
    }

     public Integer getPackage_ID() {
        return Package_ID;
    }

    public void setPackage_ID(Integer Package_ID) {
        this.Package_ID = Package_ID;
    }

    public double getAmount() {
        return Amount;
    }

    public void setAmount(double Amount) {
        this.Amount = Amount;
    }

    public String getPayment_Method() {
        return Payment_Method;
    }

    public void setPayment_Method(String Payment_Method) {
        this.Payment_Method = Payment_Method;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

    public Timestamp getTransaction_Time() {
        return Transaction_Time;
    }

    public void setTransaction_Time(Timestamp Transaction_Time) {
        this.Transaction_Time = Transaction_Time;
    }
    
    
}
