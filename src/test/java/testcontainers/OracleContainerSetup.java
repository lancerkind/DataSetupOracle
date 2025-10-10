package testcontainers;

import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

public class OracleContainerSetup {
    private static OracleContainer container;
    private static volatile boolean started = false;

    public static synchronized void ensureStarted() {
        if (started) return;
        if (container == null) {
            // Use Oracle Free image; tag can be adjusted if needed
            DockerImageName image = DockerImageName.parse("gvenzl/oracle-free:23-slim");
            container = new OracleContainer(image)
                    .withInitScript("db/00_init.sql");
        }
        container.start();
        started = true;
        // Stop container on JVM shutdown as a safety net
        Runtime.getRuntime().addShutdownHook(new Thread(OracleContainerSetup::ensureStopped));
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
