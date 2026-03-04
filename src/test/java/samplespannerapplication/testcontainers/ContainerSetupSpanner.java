package samplespannerapplication.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ContainerSetupSpanner {
    private static GenericContainer<?> container;
    private static volatile boolean started = false;

    public static synchronized void ensureStarted() {
        if (started) return;
        if (container == null) {
            container = new GenericContainer<>(DockerImageName.parse("gcr.io/cloud-spanner-emulator/emulator:1.5.47"))
                    .withExposedPorts(9010, 9020);
        }
        container.start();
        started = true;
        runInitSql("dbspanner/00_init.sql");
        Runtime.getRuntime().addShutdownHook(new Thread(ContainerSetupSpanner::ensureStopped));
    }

    private static void runInitSql(String classpathResource) {
        try (Connection conn = DriverManager.getConnection(getJdbcUrl())) {
            executeSqlScript(conn, classpathResource);
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
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }
                sb.append(line).append('\n');
                if (trimmed.endsWith(";")) {
                    executeStatement(conn, sb.toString());
                    sb.setLength(0);
                }
            }
            if (sb.length() > 0) {
                executeStatement(conn, sb.toString());
            }
        }
    }

    private static void executeStatement(Connection conn, String sql) throws SQLException {
        String toExec = sql.trim();
        if (toExec.endsWith(";")) {
            toExec = toExec.substring(0, toExec.length() - 1);
        }
        try (Statement st = conn.createStatement()) {
            st.execute(toExec);
        }
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
        String host = container.getHost();
        Integer port = container.getMappedPort(9010);
        return String.format("jdbc:cloudspanner://%s:%d/projects/test-project/instances/test-instance/databases/test-db?autoConfigEmulator=true",
                host, port);
    }

    public static String getUsername() {
        return ""; // Not needed for emulator
    }

    public static String getPassword() {
        return ""; // Not needed for emulator
    }

    public static String getDriverClassName() {
        return "com.google.cloud.spanner.jdbc.JdbcDriver";
    }
}
