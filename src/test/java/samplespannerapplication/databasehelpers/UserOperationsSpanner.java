package samplespannerapplication.databasehelpers;

import samplespannerapplication.testcontainers.ContainerSetupSpanner;

import java.sql.*;
import java.util.*;

public class UserOperationsSpanner {

    private static Connection openConnection() throws Exception {
        Class.forName(ContainerSetupSpanner.getDriverClassName());
        Connection conn = DriverManager.getConnection(ContainerSetupSpanner.getJdbcUrl());
        // Spanner JDBC driver supports auto-commit by default, 
        // but we'll manage transactions explicitly if needed.
        return conn;
    }

    public static String insertUser(Connection conn, String username, int age) throws Exception {
        boolean localConn = false;
        if (conn == null) {
            conn = openConnection();
            localConn = true;
        }
        String uuid = UUID.randomUUID().toString();
        String sql = "INSERT INTO USERS (UUID, USERNAME, AGE) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setString(2, username);
            ps.setInt(3, age);
            ps.executeUpdate();
            return uuid;
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    public static List<Map<String, Object>> getUser(Connection conn, String username) throws Exception {
        boolean localConn = false;
        if (conn == null) {
            conn = openConnection();
            localConn = true;
        }
        String sql = "SELECT UUID, USERNAME, AGE FROM USERS WHERE USERNAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        String key = md.getColumnLabel(i);
                        if (key == null || key.isEmpty()) key = md.getColumnName(i);
                        row.put(key.toUpperCase(), rs.getObject(i));
                    }
                    rows.add(row);
                }
                return rows;
            }
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    public static int deleteUser(Connection conn, String username) throws Exception {
        boolean localConn = false;
        if (conn == null) {
            conn = openConnection();
            localConn = true;
        }
        String sql = "DELETE FROM USERS WHERE USERNAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            int count = ps.executeUpdate();
            return count;
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    public static void truncateUsers() throws Exception {
        try (Connection conn = openConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM USERS WHERE 1=1");
        }
    }
}
