package se.kth.controller;

import java.sql.Connection;
import java.sql.SQLException;

import se.kth.integration.ActivityDAO;
import se.kth.integration.AllocationDAO;
import se.kth.integration.CourseInstanceDAO;
import se.kth.integration.DBHandler;
import se.kth.model.CostData;

public class TeachingManager {

    
    private final DBHandler dbHandler = new DBHandler();
    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final CourseInstanceDAO courseDAO = new CourseInstanceDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();



public String computeTeachingCost(int instanceId) throws Exception {
        Connection conn = null;
        try {
            conn = dbHandler.getConnection(); 

            CostData data = courseDAO.getCourseCostRawData(instanceId, conn);
            
            double plannedCost = data.plannedHoursWithFactor * CostData.AVG_HOURLY_RATE;
            double allocatedCost = data.allocatedHoursWithFactor * CostData.AVG_HOURLY_RATE;
            
            return String.format(
                "--- Rapport för %s (%s) ---\n" +
                "Antagen Månadslön: %.0f kr (Timlön: %.2f kr/h)\n\n" +
                "1. PLANERAD Kostnad: %.1f timmar * %.2f kr/h = %.2f SEK\n" +
                "2. FAKTISK Kostnad:  %.1f timmar * %.2f kr/h = %.2f SEK\n",
                data.courseCode, data.period,
                CostData.AVG_MONTHLY_SALARY, CostData.AVG_HOURLY_RATE,
                data.plannedHoursWithFactor, CostData.AVG_HOURLY_RATE, plannedCost,
                data.allocatedHoursWithFactor, CostData.AVG_HOURLY_RATE, allocatedCost,
                (plannedCost > 0 ? (allocatedCost / plannedCost) * 100 : 0.0)
            );

        } catch (SQLException e) {
            throw new Exception("Kunde inte hämta kostnadsdata: " + e.getMessage(), e);
        } finally {
            dbHandler.closeConnection(conn);
        }
    }

public String increaseStudentsAndShowCost(int instanceId, int increaseBy) throws Exception {
        Connection conn = null;
        StringBuilder output = new StringBuilder();
        
        try {
            conn = dbHandler.getConnection();

            CostData dataBefore = courseDAO.getCourseCostRawData(instanceId, conn);

            double costBefore = dataBefore.allocatedHoursWithFactor * CostData.AVG_HOURLY_RATE;

            output.append(String.format("--- Rapport för %s (%s) ---\n", dataBefore.courseCode, dataBefore.period));
            output.append(String.format("Faktisk Kostnad FÖRE ökning: %.2f SEK\n", costBefore));

            courseDAO.increaseStudents(instanceId, increaseBy, conn);
            

            conn.commit(); 
            output.append("\nAntal studenter ökat med ").append(increaseBy).append(". COMMIT.\n\n");

            Connection postConn = null;
            try {
                postConn = dbHandler.getConnection();
                CostData dataAfter = courseDAO.getCourseCostRawData(instanceId, postConn);

                double costAfter = dataAfter.allocatedHoursWithFactor * CostData.AVG_HOURLY_RATE;
                
                output.append(String.format("Faktisk Kostnad EFTER ökning: %.2f SEK\n", costAfter));
            } finally {
                dbHandler.closeConnection(postConn);
            }
            
            return output.toString();

        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw new Exception("Studentökning misslyckades. ROLLBACK: " + e.getMessage(), e);
        } finally {
            dbHandler.closeConnection(conn);
        }
    }
    

    
public String addNewActivityAndAllocate(int instanceId, String teacherId) throws Exception {
        Connection conn = null;
        try {
            conn = dbHandler.getConnection(); 
            

            allocationDAO.lockTeacher(teacherId, conn);


            boolean alreadyOnCourse = allocationDAO.isTeacherAllocatedToInstance(teacherId, instanceId, conn);
            

            if (alreadyOnCourse) {

                throw new Exception("Läraren (" + teacherId + ") är redan allokerad till denna kursinstans.");
            }


            String period = courseDAO.getStudyPeriod(instanceId, conn);
            int year = courseDAO.getStudyYear(instanceId, conn);
            int currentCourses = allocationDAO.countTeacherAllocationsForPeriod(teacherId, period, year, conn);

            if (currentCourses >= 4) {
                throw new Exception("Regelbrott: Läraren (" + teacherId + ") har redan 4 kurser i P" + period + " " + year + ".");
            }


            int typeId = activityDAO.getOrCreateActivityType("Exercise", conn);
            int plannedActivityId = activityDAO.createPlannedActivity(instanceId, typeId, 10, conn); 
            allocationDAO.createAllocation(teacherId, plannedActivityId, conn);

            conn.commit(); 
            
            return activityDAO.displayNewAllocation(plannedActivityId, conn);

        } catch (Exception e) {
            if (conn != null) conn.rollback();

            throw new Exception("Rollback" + e.getMessage());
        } finally {
            dbHandler.closeConnection(conn);
        }
    }
    public void allocateTeacher(String teacherId, int instanceId) throws Exception {
        Connection conn = null;
        try {
            conn = dbHandler.getConnection(); 
            
            
            allocationDAO.lockTeacher(teacherId, conn); 

            
            String period = courseDAO.getStudyPeriod(instanceId, conn);
            int year = courseDAO.getStudyYear(instanceId, conn);

            
            int currentCourses = allocationDAO.countTeacherAllocationsForPeriod(teacherId, period, year, conn);

            if (currentCourses >= 4) {
                throw new Exception("Läraren (" + teacherId + ") har redan " + currentCourses + 
                                    " kurser i period " + period + " under år " + year + ". Gränsen är 4.");
            }

            
            int typeId = activityDAO.getOrCreateActivityType("Lecture", conn);
            int plannedActivityId = activityDAO.createPlannedActivity(instanceId, typeId, 20, conn);
            allocationDAO.createAllocation(teacherId, plannedActivityId, conn);

            conn.commit(); 
            System.out.println("Allokering lyckades: " + teacherId + "  Instance " + instanceId + 
                               " (Period " + period + ", År " + year + "). COMMIT.");

        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
                System.err.println("ROLLBACK");
            }
            throw e; 
        } finally {
            dbHandler.closeConnection(conn);
        }
    }
    

    public void deallocateTeacher(String teacherId, int instanceId) throws Exception {
        Connection conn = null;
        try {
            conn = dbHandler.getConnection();

            allocationDAO.deleteAllocation(teacherId, instanceId, conn); 

            conn.commit();
            System.out.println("Deallokering lyckades: " + teacherId + " borttagen från Instance " + instanceId);

        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
                System.err.println("ROLLBACK");
            }
            throw e;
        } finally {
            dbHandler.closeConnection(conn);
        }
    }

}
