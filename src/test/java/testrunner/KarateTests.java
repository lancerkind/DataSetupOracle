package testrunner;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class KarateTests {

    @BeforeAll
    static void beforeAll() {
        // Ensure the Oracle container is started before any Karate feature runs
        testcontainers.OracleContainerSetup.ensureStarted();
    }

    @Karate.Test
    Karate testAll() {
        // Run all features under classpath:features
        return Karate.run("classpath:features");
    }

    @AfterAll
    static void afterAll() {
        testcontainers.OracleContainerSetup.ensureStopped();
    }
}
