
package se.kth.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AllocationDAO {

public int countTeacherAllocationsForPeriod(String teacherId, String period, int year, Connection conn) throws SQLException {

        String sql = "SELECT COUNT(DISTINCT pa.instance_id) " +
                     "FROM planned_employee pe " +
                     "JOIN planned_activity pa ON pe.planned_activity_id = pa.planned_activity_id " +
                     "JOIN course_instance ci ON pa.instance_id = ci.instance_id " +
                     "WHERE pe.employment_id = ? AND ci.study_period = ? AND ci.study_year = ?";
                     
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, teacherId);
            stmt.setString(2, period);
            stmt.setInt(3, year);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        }
    }



    public void createAllocation(String teacherId, int plannedActivityId, Connection conn) throws SQLException {

        String sql = "INSERT INTO planned_employee (employment_id, planned_activity_id) VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, teacherId);
            stmt.setInt(2, plannedActivityId);
            stmt.executeUpdate();
        }
    }

public void deleteAllocation(String teacherId, int instanceId, Connection conn) throws SQLException {
        
        String sql = "DELETE FROM planned_employee pe " +
                     "USING planned_activity pa " +
                     "WHERE pe.planned_activity_id = pa.planned_activity_id " +
                     "AND pe.employment_id = ? AND pa.instance_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, teacherId); 
            stmt.setInt(2, instanceId);
            
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                System.out.println("(Info: Ingen allokering hittades att ta bort för lärare " + teacherId + " på instance " + instanceId + ")");
            }
        }
    }
public void lockTeacher(String teacherId, Connection conn) throws SQLException {

        String sql = "SELECT employment_id FROM employee WHERE employment_id = ? FOR UPDATE";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Kunde inte hitta/låsa lärare med ID: " + teacherId);
            }
        }
    }
public boolean isTeacherAllocatedToInstance(String teacherId, int instanceId, Connection conn) throws SQLException {
        String sql = "SELECT 1 " +
                     "FROM planned_employee pe " +
                     "JOIN planned_activity pa ON pe.planned_activity_id = pa.planned_activity_id " +
                     "WHERE pe.employment_id = ? AND pa.instance_id = ?";
                     
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, teacherId);
            stmt.setInt(2, instanceId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); 
            }
        }
    }
}