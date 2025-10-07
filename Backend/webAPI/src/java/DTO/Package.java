package DTO;

public class Package {
    private int packageId;
    private String name;
    private String description;
    private double price;

    public Package() {}

    public Package(int packageId, String name, String description, double price) {
        this.packageId = packageId;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "PackageDTO{" +
                "packageId=" + packageId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }
}
