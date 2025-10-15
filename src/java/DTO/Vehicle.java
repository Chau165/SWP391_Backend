package DTO;

public class Vehicle {

    private int Vehicle_ID;
    private int User_ID;
    private int Model_ID;
    private String Vin;
    private String License_Plate;

    // Thêm thuộc tính mở rộng để hiển thị từ bảng Vehicle_Model
    private String Model_Name;
    private String Brand;
    private String Battery_Type;

    public Vehicle() {
    }

    public Vehicle(int Vehicle_ID, int User_ID, int Model_ID, String Vin, String License_Plate) {
        this.Vehicle_ID = Vehicle_ID;
        this.User_ID = User_ID;
        this.Model_ID = Model_ID;
        this.Vin = Vin;
        this.License_Plate = License_Plate;
    }

    public int getVehicle_ID() {
        return Vehicle_ID;
    }

    public void setVehicle_ID(int vehicle_ID) {
        Vehicle_ID = vehicle_ID;
    }

    public int getUser_ID() {
        return User_ID;
    }

    public void setUser_ID(int user_ID) {
        User_ID = user_ID;
    }

    public int getModel_ID() {
        return Model_ID;
    }

    public void setModel_ID(int model_ID) {
        Model_ID = model_ID;
    }

    public String getVin() {
        return Vin;
    }

    public void setVin(String vin) {
        Vin = vin;
    }

    public String getLicense_Plate() {
        return License_Plate;
    }

    public void setLicense_Plate(String license_Plate) {
        License_Plate = license_Plate;
    }

    public String getModel_Name() {
        return Model_Name;
    }

    public void setModel_Name(String model_Name) {
        Model_Name = model_Name;
    }

    public String getBrand() {
        return Brand;
    }

    public void setBrand(String brand) {
        Brand = brand;
    }

    public String getBattery_Type() {
        return Battery_Type;
    }

    public void setBattery_Type(String battery_Type) {
        Battery_Type = battery_Type;
    }
}
