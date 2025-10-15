package DTO;

import java.sql.Timestamp;

public class Booking {
    private int booking_ID;
    private int user_ID;
    private int vehicle_ID;
    private int package_ID;

    private int station_ID;           // song song với ChargingStation_ID
    private int chargingStation_ID;   // id từ bảng Charging_Station
    private int slot_ID;

    private String battery_Request;   // model pin (nếu có)
    private String status;            // Reserved | Completed | Cancelled...
    private Timestamp booking_Time;
    private Timestamp expired_Date;
    private String qr_Code;

    public int getBooking_ID() { return booking_ID; }
    public void setBooking_ID(int booking_ID) { this.booking_ID = booking_ID; }

    public int getUser_ID() { return user_ID; }
    public void setUser_ID(int user_ID) { this.user_ID = user_ID; }

    public int getVehicle_ID() { return vehicle_ID; }
    public void setVehicle_ID(int vehicle_ID) { this.vehicle_ID = vehicle_ID; }

    public int getPackage_ID() { return package_ID; }
    public void setPackage_ID(int package_ID) { this.package_ID = package_ID; }

    public int getStation_ID() { return station_ID; }
    public void setStation_ID(int station_ID) { this.station_ID = station_ID; }

    public int getChargingStation_ID() { return chargingStation_ID; }
    public void setChargingStation_ID(int chargingStation_ID) { this.chargingStation_ID = chargingStation_ID; }

    public int getSlot_ID() { return slot_ID; }
    public void setSlot_ID(int slot_ID) { this.slot_ID = slot_ID; }

    public String getBattery_Request() { return battery_Request; }
    public void setBattery_Request(String battery_Request) { this.battery_Request = battery_Request; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getBooking_Time() { return booking_Time; }
    public void setBooking_Time(Timestamp booking_Time) { this.booking_Time = booking_Time; }

    public Timestamp getExpired_Date() { return expired_Date; }
    public void setExpired_Date(Timestamp expired_Date) { this.expired_Date = expired_Date; }

    public String getQr_Code() { return qr_Code; }
    public void setQr_Code(String qr_Code) { this.qr_Code = qr_Code; }
}
