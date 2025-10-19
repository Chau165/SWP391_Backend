package DTO;

public class Battery {
    private int batteryId;
    private String serialNumber;
    private double resistance;
    private double soH;
    private int typeId; // FK -> BatteryType.Type_ID

   

    // ====== Getter & Setter ======

    public int getBatteryId() { return batteryId; }
    public void setBatteryId(int batteryId) { this.batteryId = batteryId; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public double getResistance() { return resistance; }
    public void setResistance(double resistance) { this.resistance = resistance; }

    public double getSoH() { return soH; }
    public void setSoH(double soH) { this.soH = soH; }

    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }

  
}
