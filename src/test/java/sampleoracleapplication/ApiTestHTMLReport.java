package sampleoracleapplication;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class ApiTestHTMLReport {

    @BeforeAll
    static void beforeAll() {
        // Ensure the Oracle container is started before any Karate feature runs
        testcontainers.OracleContainerSetup.ensureStarted();
    }

    @Karate.Test
    Karate testAllFeatures() {
        // pass the path to your test features directory here:
        return Karate.run("classpath:sampleoracleapplication/");
    }

    @AfterAll
    static void afterAll() {
        testcontainers.OracleContainerSetup.ensureStopped();
    }
}
