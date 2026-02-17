
package se.kth.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHandler {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/sem2";
    private static final String USER = "postgres";
    private static final String PASS = "Spindel3017";


    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

        conn.setAutoCommit(false); 
        
        return conn;
    }

    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Problem stängning" + e.getMessage());
            }
        }
    }
}