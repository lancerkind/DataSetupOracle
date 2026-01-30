package sampleoracleapplication;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import sampleoracleapplication.testcontainers.ContainerSetupOracle;

public class TestAllFeaturesTestRunner {

    @BeforeAll
    static void beforeAll() {
        // Ensure the Oracle container is started before any Karate feature runs
        ContainerSetupOracle.ensureStarted();
    }

    @Karate.Test
    Karate testAllFeatures() {
        // pass the path to your test features directory here:
        return Karate.run("classpath:sampleoracleapplication/");
    }

    @AfterAll
    static void afterAll() {
        ContainerSetupOracle.ensureStopped();
    }
}
