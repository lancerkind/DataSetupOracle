# Story 1: Add Spanner Test Container Features

## Objectives
- Implement Spanner as an alternative database backend (similar to what was done for Oracle)
- Enable testing against Spanner using testcontainers

## Spanner Modifications
- Copy feature files as-is from Oracle to Spanner directory
- Change only one line in each feature file: the Java.type() reference
- All the business logic remains identical because the UserOperationsSpanner class will handle the Spanner-specific 
SQL internally. Its interface will be the same as the Oracle version.
- The schemas that are created in the initialization scripts will be identical to the Oracle schemas, though the actual script content will be custom to Spanner.
- karate-config.js updated to use the Spanner JDBC URL and credentials.
- Create a new ContainerSetupSpanner class with a Spanner JDBC URL instead of Oracle's
- Rewrite 00_init.sql for Spanner (no sequences, simplified or removed procedures)
- Move the INSERT_USER, GET_USER, DELETE_USER logic into the UserOperationsSpanner Java class instead of relying on stored procedures
- Update the spanner feature files to call Java methods instead of database procedures
- Update the build.gradle file to add the Spanner dependencies
- Spanner feature files should go in: `src/test/resources/samplespannerapplication/admin-operations` and `src/test/resources/samplespannerapplication/user-operations`
  - Change only one line in each feature file: the Java.type() reference
    from `Java.type('sampleoracleapplication.databasehelpers.UserOperations')`
    to `Java.type('samplespannerapplication.databasehelpers.UserOperationsSpanner')`

## Database Schema
dbspanner/00_init.sql contains what's needed for table creation.
The schema is identical to the Oracle schema except for the following:
- Spanner does not support sequences, so the USERS table does not have an id column.

### UserOperationsSpanner Implementation Details
- `insertUser(username, age)` should return UUID as a String
- `getUser(username)` should return ResultSet as List<Map> (same as Oracle version)
- `deleteUser(username)` should return count of deleted rows


### Table: USERS
| Column   | Type         | Constraints |
|----------|--------------|-------------|
| UUID     | STRING(36)   | primary key |
| USERNAME | VARCHAR2(50) | not null    |
| AGE      | INT64        | NOT NULL    |
Create an index on USERNAME.
Let Spanner generate the UUID.

## Acceptance Criteria
1. In a directory called "samplespannerapplication," create Spanner feature files for the following using the same 
operations mentioned in the Oracle feature files:
    - User operations (create, read, update, delete operations)
    - Admin operations (schema exploration, cleanup)
2. Create `ContainerSetupSpanner` class following the pattern of `ContainerSetupOracle`
3. Create `SpannerTestRunner` test class to execute feature files for Spanner that are in `src/test/resources/samplespannerapplication`
4. Copy existing Oracle feature files to use as test for Spanner
5. Update build.gradle with required Spanner dependencies
6. Create Spanner initialization scripts that will setup the environment so it will pass the tests run in SpannerTestRunner `src/test/resources/dbspanner/` 

## Dependency Versions
- Use spanner test container version 1.5.47.
- Use version 5.0.0 of Google cloud spanner client library

## Definition of Done
- All tests pass

## References
- See: `OracleContainerSetup.java` and `OracleTests.java` for implementation pattern