package DTO;

public class Package {
    private int packageId;
    private String name;
    private String description;
    private double price;
    private double requiredSoH;
    private int minSoH;
    private int maxSoH;

    public Package() {}

    public Package(int packageId, String name, String description, double price, double requiredSoH, int minSoH, int maxSoH) {
        this.packageId = packageId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.requiredSoH = requiredSoH;
        this.minSoH = minSoH;
        this.maxSoH = maxSoH;
    }

    public Package(int packageId, String name, String description, double price, double requiredSoH) {
        this.packageId = packageId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.requiredSoH = requiredSoH;
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

    public double getRequiredSoH() {
        return requiredSoH;
    }

    public void setRequiredSoH(double requiredSoH) {
        this.requiredSoH = requiredSoH;
    }

    public int getMinSoH() {
        return minSoH;
    }

    public void setMinSoH(int minSoH) {
        this.minSoH = minSoH;
    }

    public int getMaxSoH() {
        return maxSoH;
    }

    public void setMaxSoH(int maxSoH) {
        this.maxSoH = maxSoH;
    }
}
