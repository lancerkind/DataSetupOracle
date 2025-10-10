package database;

import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.*;

public class UserOperations {

    // Convenience: open a new Connection using Testcontainers config
    private static Connection openConnection() throws Exception {
        Class.forName(testcontainers.OracleContainerSetup.getDriverClassName());
        return DriverManager.getConnection(
                testcontainers.OracleContainerSetup.getJdbcUrl(),
                testcontainers.OracleContainerSetup.getUsername(),
                testcontainers.OracleContainerSetup.getPassword());
    }

    public static long callInsertUser(Connection conn, String username, int age) throws Exception {
        boolean localConn = false;
        if (conn == null) {
            conn = openConnection();
            localConn = true;
        }
        try (CallableStatement cs = conn.prepareCall("{ call INSERT_USER(?, ?, ?) }")) {
            cs.setString(1, username);
            cs.setInt(2, age);
            cs.registerOutParameter(3, Types.NUMERIC);
            cs.execute();
            long id = cs.getLong(3);
            if (localConn) conn.commit();
            return id;
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    public static List<Map<String, Object>> callGetUser(Connection conn, String username) throws Exception {
        boolean localConn = false;
        if (conn == null) {
            conn = openConnection();
            localConn = true;
        }
        ResultSet rs = null;
        try (CallableStatement cs = conn.prepareCall("{ ? = call GET_USER(?) }")) {
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.setString(2, username);
            cs.execute();
            rs = (ResultSet) cs.getObject(1);
            List<Map<String, Object>> rows = new ArrayList<>();
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= cols; i++) {
                    String key = md.getColumnLabel(i);
                    if (key == null || key.isEmpty()) key = md.getColumnName(i);
                    // Keep Oracle uppercase column names as requested
                    row.put(key, rs.getObject(i));
                }
                rows.add(row);
            }
            return rows;
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (localConn && conn != null) conn.close();
        }
    }

    public static int callDeleteUser(Connection conn, String username) throws Exception {
        boolean localConn = false;
        if (conn == null) {
            conn = openConnection();
            localConn = true;
        }
        try (CallableStatement cs = conn.prepareCall("{ call DELETE_USER(?, ?) }")) {
            cs.setString(1, username);
            cs.registerOutParameter(2, Types.NUMERIC);
            cs.execute();
            int count = cs.getInt(2);
            if (localConn) conn.commit();
            return count;
        } finally {
            if (localConn && conn != null) conn.close();
        }
    }

    // Utility for test isolation: truncate USERS between scenarios
    public static void truncateUsers() throws Exception {
        try (Connection conn = openConnection(); Statement st = conn.createStatement()) {
            try {
                st.executeUpdate("TRUNCATE TABLE USERS");
            } catch (SQLException e) {
                // If TRUNCATE fails due to locks, fallback to DELETE
                st.executeUpdate("DELETE FROM USERS");
            }
            conn.commit();
        }
    }
}
