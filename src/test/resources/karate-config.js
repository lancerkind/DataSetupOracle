function fn() {
  var Setup = Java.type('sampleoracleapplication.testcontainers.ContainerSetupOracle');
  return {
    jdbcUrl: Setup.getJdbcUrl(),
    username: Setup.getUsername(),
    password: Setup.getPassword()
  };
}
