package se.kth.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ActivityDAO {

    public int getOrCreateActivityType(String activityName, Connection conn) throws SQLException {
        String checkSql = "SELECT teaching_activity_id FROM teaching_activity WHERE activity_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setString(1, activityName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        
        String insertSql = "INSERT INTO teaching_activity (activity_name) VALUES (?) RETURNING teaching_activity_id";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, activityName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Kunde inte hämta ID för aktivitetstyp: " + activityName);
    }

    public int createPlannedActivity(int instanceId, int teachingActivityId, int hours, Connection conn) throws SQLException {
        String sql = "INSERT INTO planned_activity (instance_id, teaching_activity_id, planned_hours) " +
                     "VALUES (?, ?, ?) RETURNING planned_activity_id";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instanceId);
            stmt.setInt(2, teachingActivityId);
            stmt.setInt(3, hours);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Kunde inte skapa planned_activity.");
    }
    
public String displayNewAllocation(int plannedActivityId, Connection conn) throws SQLException {
        
        String sql = 
            "SELECT cl.course_code, ci.study_period, p.first_name, p.last_name, ta.activity_name " +
            "FROM planned_employee pe " +
            "JOIN employee e ON pe.employment_id = e.employment_id " +
            "JOIN person p ON e.person_id = p.person_id " +
            "JOIN planned_activity pa ON pe.planned_activity_id = pa.planned_activity_id " +
            "JOIN teaching_activity ta ON pa.teaching_activity_id = ta.teaching_activity_id " +
            "JOIN course_instance ci ON pa.instance_id = ci.instance_id " +
            "JOIN course_layout cl ON ci.course_layout_id = cl.course_layout_id " +
            "WHERE pe.planned_activity_id = ?";

        StringBuilder sb = new StringBuilder();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, plannedActivityId);
            try (ResultSet rs = stmt.executeQuery()) {
                sb.append("\n--- RESULTAT: NY AKTIVITET & ALLOKERING ---\n");
                while (rs.next()) {
                     sb.append(String.format("Kurs: %s (%s) | Lärare: %s %s | Aktivitet: %s\n", 
                            rs.getString("course_code"), 
                            rs.getString("study_period"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("activity_name")));
                }
            }
        }
        return sb.toString();
    }
}
