package DTO;

/**
 * DTO đại diện cho trạm đổi pin
 * Bổ sung tọa độ để hiển thị trên Mapbox
 */
public class Station {
    private int Station_ID;
    private String Name;
    private String Address;
    private Double Latitude;   // vĩ độ
    private Double Longitude;  // kinh độ

    public Station() {
    }

    public Station(int Station_ID, String Name, String Address) {
        this.Station_ID = Station_ID;
        this.Name = Name;
        this.Address = Address;
    }

    public Station(int Station_ID, String Name, String Address, Double Latitude, Double Longitude) {
        this.Station_ID = Station_ID;
        this.Name = Name;
        this.Address = Address;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
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

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double Latitude) {
        this.Latitude = Latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double Longitude) {
        this.Longitude = Longitude;
    }

    @Override
    public String toString() {
        return "Station{" +
                "Station_ID=" + Station_ID +
                ", Name='" + Name + '\'' +
                ", Address='" + Address + '\'' +
                ", Latitude=" + Latitude +
                ", Longitude=" + Longitude +
                '}';
    }
}
