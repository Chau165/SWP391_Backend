package DTO;

import java.time.LocalDate;

public class DispatchLog {
    private int ID;
    private int Station_Request_ID;
    private int Station_Respond_ID;
    private int BatteryType_Request_ID;
    private int Quantity_Type_Good;
    private int Quantity_Type_Average;
    private int Quantity_Type_Bad;
    private LocalDate Request_Time;
    private LocalDate Respond_Time;
    private String Status;

    public DispatchLog() {}

    public DispatchLog(int ID, int Station_Request_ID, int Station_Respond_ID, int BatteryType_Request_ID,
                       int Quantity_Type_Good, int Quantity_Type_Average, int Quantity_Type_Bad,
                       LocalDate Request_Time, LocalDate Respond_Time, String Status) {
        this.ID = ID;
        this.Station_Request_ID = Station_Request_ID;
        this.Station_Respond_ID = Station_Respond_ID;
        this.BatteryType_Request_ID = BatteryType_Request_ID;
        this.Quantity_Type_Good = Quantity_Type_Good;
        this.Quantity_Type_Average = Quantity_Type_Average;
        this.Quantity_Type_Bad = Quantity_Type_Bad;
        this.Request_Time = Request_Time;
        this.Respond_Time = Respond_Time;
        this.Status = Status;
    }

    public int getID() { return ID; }
    public void setID(int ID) { this.ID = ID; }

    public int getStation_Request_ID() { return Station_Request_ID; }
    public void setStation_Request_ID(int Station_Request_ID) { this.Station_Request_ID = Station_Request_ID; }

    public int getStation_Respond_ID() { return Station_Respond_ID; }
    public void setStation_Respond_ID(int Station_Respond_ID) { this.Station_Respond_ID = Station_Respond_ID; }

    public int getBatteryType_Request_ID() { return BatteryType_Request_ID; }
    public void setBatteryType_Request_ID(int BatteryType_Request_ID) { this.BatteryType_Request_ID = BatteryType_Request_ID; }

    public int getQuantity_Type_Good() { return Quantity_Type_Good; }
    public void setQuantity_Type_Good(int Quantity_Type_Good) { this.Quantity_Type_Good = Quantity_Type_Good; }

    public int getQuantity_Type_Average() { return Quantity_Type_Average; }
    public void setQuantity_Type_Average(int Quantity_Type_Average) { this.Quantity_Type_Average = Quantity_Type_Average; }

    public int getQuantity_Type_Bad() { return Quantity_Type_Bad; }
    public void setQuantity_Type_Bad(int Quantity_Type_Bad) { this.Quantity_Type_Bad = Quantity_Type_Bad; }

    public LocalDate getRequest_Time() { return Request_Time; }
    public void setRequest_Time(LocalDate Request_Time) { this.Request_Time = Request_Time; }

    public LocalDate getRespond_Time() { return Respond_Time; }
    public void setRespond_Time(LocalDate Respond_Time) { this.Respond_Time = Respond_Time; }

    public String getStatus() { return Status; }
    public void setStatus(String Status) { this.Status = Status; }
}
