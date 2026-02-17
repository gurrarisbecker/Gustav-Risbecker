package se.kth.model;

public class CostData {
    public final String courseCode;
    public final String period;
    public final double plannedHoursWithFactor;
    public final double allocatedHoursWithFactor;
    
    public static final double AVG_MONTHLY_SALARY = 40000.0;
    public static final double HOURS_PER_MONTH = 160.0;
    public static final double AVG_HOURLY_RATE = AVG_MONTHLY_SALARY / HOURS_PER_MONTH; 

    public CostData(String code, String period, double planned, double allocated) {
        this.courseCode = code;
        this.period = period;
        this.plannedHoursWithFactor = planned;
        this.allocatedHoursWithFactor = allocated;
    }
}
