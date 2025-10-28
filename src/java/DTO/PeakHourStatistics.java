package DTO;

/**
 * DTO cho thống kê giờ cao điểm của swap transactions
 */
public class PeakHourStatistics {
    private String timeSlot;      // Khung giờ (VD: "00:00-01:00", "06:00-07:00")
    private int swapCount;        // Số lượt đổi pin trong khung giờ
    private double totalRevenue;  // Tổng doanh thu trong khung giờ
    private double averageFee;    // Phí trung bình
    
    public PeakHourStatistics() {
    }

    public PeakHourStatistics(String timeSlot, int swapCount, double totalRevenue, double averageFee) {
        this.timeSlot = timeSlot;
        this.swapCount = swapCount;
        this.totalRevenue = totalRevenue;
        this.averageFee = averageFee;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public int getSwapCount() {
        return swapCount;
    }

    public void setSwapCount(int swapCount) {
        this.swapCount = swapCount;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getAverageFee() {
        return averageFee;
    }

    public void setAverageFee(double averageFee) {
        this.averageFee = averageFee;
    }

    @Override
    public String toString() {
        return "PeakHourStatistics{" +
                "timeSlot='" + timeSlot + '\'' +
                ", swapCount=" + swapCount +
                ", totalRevenue=" + totalRevenue +
                ", averageFee=" + averageFee +
                '}';
    }
}
