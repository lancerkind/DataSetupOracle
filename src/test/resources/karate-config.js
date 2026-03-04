function fn() {
  var env = karate.env;
  var Setup;
  if (env == 'spanner') {
    Setup = Java.type('samplespannerapplication.testcontainers.ContainerSetupSpanner');
  } else {
    Setup = Java.type('sampleoracleapplication.testcontainers.ContainerSetupOracle');
  }
  return {
    jdbcUrl: Setup.getJdbcUrl(),
    username: Setup.getUsername(),
    password: Setup.getPassword()
  };
}
