package testcontainers;

import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class OracleContainerSetup {
    private static OracleContainer container;
    private static volatile boolean started = false;

    public static synchronized void ensureStarted() {
        if (started) return;
        if (container == null) {
            // Use Oracle Free image; tag can be adjusted if needed
            DockerImageName image = DockerImageName.parse("gvenzl/oracle-free:23-slim");
            container = new OracleContainer(image);
        }
        container.start();
        // run init SQL manually to properly handle PL/SQL blocks
        runInitSql("db/00_init.sql");
        started = true;
        // Stop container on JVM shutdown as a safety net
        Runtime.getRuntime().addShutdownHook(new Thread(OracleContainerSetup::ensureStopped));
    }

    private static void runInitSql(String classpathResource) {
        try (Connection conn = DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword())) {
            executeSqlScript(conn, classpathResource);
            // verify there are no compilation errors for created PL/SQL objects
            String errors = collectUserErrors(conn);
            if (!errors.isEmpty()) {
                throw new RuntimeException("PL/SQL compilation errors after init script:\n" + errors);
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to execute init SQL: " + classpathResource, e);
        }
    }

    private static void executeSqlScript(Connection conn, String classpathResource) throws IOException, SQLException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathResource);
        if (is == null) {
            throw new IOException("Resource not found on classpath: " + classpathResource);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            boolean inPlsql = false;
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    // skip comments/blank lines
                    continue;
                }
                if (!inPlsql && trimmed.matches("(?i)^CREATE\\s+OR\\s+REPLACE\\s+(PROCEDURE|FUNCTION)\\b.*")) {
                    inPlsql = true;
                }
                if (inPlsql && "/".equals(trimmed)) {
                    // end of PL/SQL; execute accumulated block; keep trailing semicolon
                    executeStatement(conn, sb.toString(), true);
                    sb.setLength(0);
                    inPlsql = false;
                    continue;
                }
                sb.append(line).append('\n');
                if (!inPlsql && trimmed.endsWith(";")) {
                    // simple SQL statement; strip trailing semicolon
                    executeStatement(conn, sb.toString(), false);
                    sb.setLength(0);
                }
            }
            // execute any trailing statement without delimiter
            if (sb.length() > 0) {
                // treat as simple SQL by default
                executeStatement(conn, sb.toString(), false);
            }
        }
    }

    private static void executeStatement(Connection conn, String sql, boolean keepSemicolon) throws SQLException {
        String toExec = sql.trim();
        if (toExec.endsWith(";") && !keepSemicolon) {
            toExec = toExec.substring(0, toExec.length() - 1);
        }
        try (Statement st = conn.createStatement()) {
            st.execute(toExec);
        }
    }

    private static String collectUserErrors(Connection conn) throws SQLException {
        StringBuilder out = new StringBuilder();
        String q = "SELECT NAME, TYPE, LINE, POSITION, TEXT FROM USER_ERRORS ORDER BY NAME, SEQUENCE";
        try (Statement st = conn.createStatement(); var rs = st.executeQuery(q)) {
            while (rs.next()) {
                String name = rs.getString(1);
                String type = rs.getString(2);
                int line = rs.getInt(3);
                int pos = rs.getInt(4);
                String text = rs.getString(5);
                out.append(name).append(" (").append(type).append(") line ")
                   .append(line).append(":").append(pos).append(" - ")
                   .append(text).append('\n');
            }
        }
        return out.toString();
    }

    public static synchronized void ensureStopped() {
        if (container != null && started) {
            try {
                container.stop();
            } finally {
                started = false;
            }
        }
    }

    public static String getJdbcUrl() {
        ensureStarted();
        return container.getJdbcUrl();
    }

    public static String getUsername() {
        ensureStarted();
        return container.getUsername();
    }

    public static String getPassword() {
        ensureStarted();
        return container.getPassword();
    }

    public static String getDriverClassName() {
        return "oracle.jdbc.OracleDriver";
    }
}
