function fn() {
  var Setup = Java.type('testcontainers.OracleContainerSetup');
  // Accessing will ensure container is started
  var config = {
    jdbcUrl: Setup.getJdbcUrl(),
    username: Setup.getUsername(),
    password: Setup.getPassword()
  };
  return config;
}
