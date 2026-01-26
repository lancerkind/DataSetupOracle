# Spanner enablement  
Please add to this codebase an example that uses Spanner in a test container.  Similar to the Oracle example:
- create a new feature files that mirror the Oracle example
- create a new test class to run the feature files

# Story 1: Add Spanner Test Container Support

## Objectives
- Implement Spanner as an alternative database backend (similar to Oracle)
- Enable testing against Spanner using testcontainers

## Acceptance Criteria
1. Create Spanner feature files for:
    - User operations (create, read, update, delete operations)
    - Admin operations (schema exploration, cleanup)
2. Create `SpannerContainerSetup` class following the pattern of `OracleContainerSetup`
3. Create `SpannerTests` test class to execute feature files
4. Update build.gradle with required Spanner dependencies
5. Create Spanner initialization scripts in `src/test/resources/db/spanner/`

## Technical Details
- Use [specific-testcontainer-version]
- Spanner feature files should go in: `src/test/resources/sampleoracleapplication/spanner-operations/`
- Follow Karate framework conventions matching existing Oracle tests
- Ensure Google Cloud Spanner API compatibility

## References
- See: `OracleContainerSetup.java` and `OracleTests.java` for implementation pattern