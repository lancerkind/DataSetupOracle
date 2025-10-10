function fn() {
  var Setup = Java.type('testcontainers.OracleContainerSetup');
  return {
    jdbcUrl: Setup.getJdbcUrl(),
    username: Setup.getUsername(),
    password: Setup.getPassword()
  };
}
