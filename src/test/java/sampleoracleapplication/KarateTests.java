package sampleoracleapplication;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import sampleoracleapplication.testcontainers.OracleContainerSetup;

public class KarateTests {

    @BeforeAll
    static void beforeAll() {
        // Ensure the Oracle container is started before any Karate feature runs
        OracleContainerSetup.ensureStarted();
    }

    @Karate.Test
    Karate testUserOperations() {
        return Karate.run("classpath:sampleoracleapplication/user-operations/");
    }


    @Karate.Test
    Karate testAdminOperations() {
        return Karate.run("classpath:sampleoracleapplication/admin-operations");
    }

    @AfterAll
    static void afterAll() {
        OracleContainerSetup.ensureStopped();
    }
}
