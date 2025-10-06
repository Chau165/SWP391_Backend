/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

import java.sql.Date;
import javafx.scene.chart.PieChart;

/**
 *
 * @author Surface
 */
public class Booking {
    public int Booking_ID;
    public int Vehicle_ID;
    public String Battery_Request;
    public int Package_ID;
    public int User_ID;
    public int Station_ID;
    public String Status;
    public String Qr_Code;
    public Date Booking_Time;
    public Date Expired_Date;

    public Booking() {
    }

    public Booking(int Booking_ID, int Vehicle_ID, String Battery_Request, int Package_ID, int User_ID, int Station_ID, String Status, String Qr_Code, Date Booking_Time, Date Expired_Date) {
        this.Booking_ID = Booking_ID;
        this.Vehicle_ID = Vehicle_ID;
        this.Battery_Request = Battery_Request;
        this.Package_ID = Package_ID;
        this.User_ID = User_ID;
        this.Station_ID = Station_ID;
        this.Status = Status;
        this.Qr_Code = Qr_Code;
        this.Booking_Time = Booking_Time;
        this.Expired_Date = Expired_Date;
    }
    
}
