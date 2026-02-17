package se.kth.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import se.kth.model.CostData;

public class CourseInstanceDAO {

    public int findInstanceId(String courseCode, String period, Connection conn) throws SQLException {
        String sql = "SELECT ci.instance_id FROM course_instance ci " +
                     "JOIN course_layout cl ON ci.course_layout_id = cl.course_layout_id " +
                     "WHERE cl.course_code = ? AND ci.study_period = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, period);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Hittade inte kursinstans ID för " + courseCode + " i period " + period);
    }

    public String getStudyPeriod(int instanceId, Connection conn) throws SQLException {
        String sql = "SELECT study_period FROM course_instance WHERE instance_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instanceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        throw new SQLException("Kunde inte hitta study_period för instance_id: " + instanceId);
    }

    public int getStudyYear(int instanceId, Connection conn) throws SQLException {
        String sql = "SELECT study_year FROM course_instance WHERE instance_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instanceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Kunde inte hitta study_year för instance_id: " + instanceId);
    }

public CostData getCourseCostRawData(int instanceId, Connection conn) throws SQLException {
        
        String sql = 
            "SELECT " +
            "   cl.course_code, " +
            "   ci.study_period, " +

            "   COALESCE(SUM( " +
            "       CASE " +
            "           WHEN ta.activity_name = 'Exam' " +
            "           THEN pa.planned_hours + (COALESCE(ta.factor, 1.0) * ci.num_students) " +
            "           ELSE pa.planned_hours * COALESCE(ta.factor, 1.0) " +                     
            "       END " +
            "   ), 0) AS total_planned, " +

            "   COALESCE(SUM( " +
            "       CASE WHEN pe.employment_id IS NOT NULL THEN " +
            "           CASE " +
            "               WHEN ta.activity_name = 'Exam' " +
            "               THEN pa.planned_hours + (COALESCE(ta.factor, 1.0) * ci.num_students) " +
            "               ELSE pa.planned_hours * COALESCE(ta.factor, 1.0) " +
            "           END " +
            "       ELSE 0 END " +
            "   ), 0) AS total_allocated " +
            
            "FROM course_instance ci " +
            "JOIN course_layout cl ON ci.course_layout_id = cl.course_layout_id " +
            "LEFT JOIN planned_activity pa ON ci.instance_id = pa.instance_id " +
            "LEFT JOIN teaching_activity ta ON pa.teaching_activity_id = ta.teaching_activity_id " +
            "LEFT JOIN planned_employee pe ON pa.planned_activity_id = pe.planned_activity_id " +
            "WHERE ci.instance_id = ? " +
            "GROUP BY ci.instance_id, cl.course_code, ci.study_period";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instanceId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new CostData(
                        rs.getString("course_code"), 
                        rs.getString("study_period"), 
                        rs.getDouble("total_planned"),
                        rs.getDouble("total_allocated")
                    );
                }
                throw new SQLException("Kunde inte hitta kursinstans ID: " + instanceId);
            }
        }
    }


    public void increaseStudents(int instanceId, int increaseBy, Connection conn) throws SQLException {
        String sql = "UPDATE course_instance SET num_students = num_students + ? WHERE instance_id = ?"; 
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, increaseBy);
            stmt.setInt(2, instanceId);
            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException("Ingen kurs uppdaterades för ID: " + instanceId);
        }
    }
}