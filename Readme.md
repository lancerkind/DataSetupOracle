# Karate Database POC - Oracle & Spanner

## Overview
This POC demonstrates how the Karate framework can be used to perform integration testing against containerized databases. It covers two primary backends:
- **Oracle Free**: Testing via stored procedures and SYS_REFCURSORs.
- **Google Cloud Spanner**: Testing via direct SQL operations and UUIDs.

Both databases are managed using **Testcontainers**, providing a clean, isolated environment for each test run.

## Technology Stack
- **Language**: Java 17
- **Build Tool**: Gradle
- **Databases**: Oracle Free, Google Cloud Spanner
- **Testing Framework**: Karate (karatelabs)
- **Key Libraries**: Testcontainers, JDBC drivers (ojdbc11, google-cloud-spanner-jdbc)

## Database Schemas

The POC implements a `USERS` table in both databases with equivalent functionality but platform-specific implementations.

### Oracle (via Stored Procedures)
| Column   | Type         | Constraints                         |
|----------|--------------|-------------------------------------|
| ID       | NUMBER       | Primary Key (via Sequence)          |
| USERNAME | VARCHAR2(50) | Not Null, Unique                    |
| AGE      | NUMBER(3)    | Not Null                            |

- **Logic**: Operations (Insert, Get, Delete) are handled via stored procedures in `02_procedures.sql`.
- **Identity**: Uses `USER_ID_SEQ`.

### Spanner (via Java Helpers)
| Column   | Type       | Constraints          |
|----------|------------|----------------------|
| UUID     | STRING(36) | Primary Key          |
| USERNAME | STRING(50) | Not Null             |
| AGE      | INT64      | Not Null             |

- **Logic**: Operations are handled by the `UserOperationsSpanner` Java class.
- **Identity**: Uses generated UUIDs.

## Project Structure
Feature files and database helpers are organized by application type:

- `src/test/java/sampleoracleapplication/`: Oracle-specific test runners, container setup, and database helpers.
- `src/test/java/samplespannerapplication/`: Spanner-specific test runners, container setup, and database helpers.
- `src/test/resources/dboracle/`: Oracle initialization scripts.
- `src/test/resources/dbspanner/`: Spanner initialization scripts.
- `src/test/resources/sampleoracleapplication/`: Oracle Karate feature files.
- `src/test/resources/samplespannerapplication/`: Spanner Karate feature files.

## Running the Tests
Run all tests using Gradle:
```bash
./gradlew test
```
Testcontainers will automatically start the required database instances, run the initialization scripts, and shut down after tests complete.

### Test Runners
- **Oracle**: `OracleTestRunner.java`
- **Spanner**: `SpannerTestRunner.java`
- **All**: `TestAllFeaturesTestRunner.java`

## Karate Integration
Karate interacts with the databases through Java interop, calling methods in `UserOperationsOracle` or `UserOperationsSpanner`. These helpers manage JDBC connections provided by the respective Testcontainers.

Example Feature Snippet (Spanner):
```cucumber
* def UserOps = Java.type('samplespannerapplication.databasehelpers.UserOperationsSpanner')
* def result = UserOps.insertUser('john_doe', 25)
```

## Gradle Dependencies (Key)
```gradle
testImplementation 'io.karatelabs:karate-junit5:1.5.1'
testImplementation 'com.oracle.database.jdbc:ojdbc11:23.9.0.25.07'
testImplementation 'org.testcontainers:oracle-free:1.21.3'
testImplementation 'com.google.cloud:google-cloud-spanner:5.0.0'
testImplementation 'com.google.cloud:google-cloud-spanner-jdbc:2.24.1'
```

## Troubleshooting
- **Docker**: Ensure Docker Desktop is running.
- **Reports**: Detailed Karate reports are generated in `build/reports/tests/test/index.html`.
- **Logging**: Configure `src/test/resources/logback-test.xml` to manage log verbosity.

# Resources
API documentation: 
https://javadoc.io/doc/io.karatelabs
https://karatelabs.github.io/karate/


# Troubleshooting (Extended)
## Question: although karate is looking for a features directory on the classpath and I have 
"features.somearea" in intelliJ, how is it finding my feature files?
### Answer: In certain circumstances Intellij really is using a directory structure beneath that is "./features/somearea/".
You'll need to look at the file structure to see what's really going on.

## Problem: Karate tests fail and I'm unclear why.
### Solution: See the test report out and "click in" until you get to staketrace.
Test report is listed on the last line displayed by the test runner.  For example:
file:///Users/<user>/IdeaProjects/DataSetupOracle/build/reports/tests/test/index.html

## The test report output has too much chatter from the test container to be practical. How can I reduce that?
### Confirm that test/resources/logback-test.xml is configured for ERROR level logging.  Confirm that a logslf4j implementation is provided, such as:
```gradle
testImplementation 'ch.qos.logback:logback-classic:1.5.20
```

## Problem: Testcontainer fails to start. Test execution logs show: /Users/<user>/.docker/run/docker.sock' does not exist.
### Solution:
Start Docker Desktop.