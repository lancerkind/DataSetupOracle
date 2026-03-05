package samplespannerapplication.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import biz.agilenoir.LiquibaseMigrations;

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
        runLiquibase();
        Runtime.getRuntime().addShutdownHook(new Thread(ContainerSetupSpanner::ensureStopped));
    }

    private static void runLiquibase() {
        new LiquibaseMigrations().run(getJdbcUrl(), "dbSpanner/changelog/changelog.xml");
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
