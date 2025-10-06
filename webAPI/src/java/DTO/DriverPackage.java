/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

import java.sql.Date;

/**
 *
 * @author Surface
 */
public class DriverPackage {
    public int ID;
    public int User_ID;
    public int Package_ID;
    public Date Start_date;
    public Date End_date;

    public DriverPackage() {
    }

    
    public DriverPackage(int ID, int User_ID, int Package_ID, Date Start_date, Date End_date) {
        this.ID = ID;
        this.User_ID = User_ID;
        this.Package_ID = Package_ID;
        this.Start_date = Start_date;
        this.End_date = End_date;
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

    public int getPackage_ID() {
        return Package_ID;
    }

    public void setPackage_ID(int Package_ID) {
        this.Package_ID = Package_ID;
    }

    public Date getStart_date() {
        return Start_date;
    }

    public void setStart_date(Date Start_date) {
        this.Start_date = Start_date;
    }

    public Date getEnd_date() {
        return End_date;
    }

    public void setEnd_date(Date End_date) {
        this.End_date = End_date;
    }
    
    
}
