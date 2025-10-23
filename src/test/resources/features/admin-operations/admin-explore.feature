Feature: Admin Explore
This is a duplicate of another feature in order to demonstrate how to organize
a large number of features.

  Background:
    * def UserOps = Java.type('database.UserOperations')
    # Ensure clean table per scenario
    * eval UserOps.truncateUsers()

  Scenario: Add a new user
    * def username = 'john_doe'
    * def age = 25
    * def id = UserOps.callInsertUser(null, username, age)
    * def rows = UserOps.callGetUser(null, username)
    * match rows == '#[1]'
    * match rows[0].USERNAME == username
    * match rows[0].AGE == age
    # Cleanup
    * def deleted = UserOps.callDeleteUser(null, username)
    * match deleted == 1

  Scenario: Delete an existing user
    * def username = 'jane_doe'
    * def age = 30
    * def id = UserOps.callInsertUser(null, username, age)
    * def deleted = UserOps.callDeleteUser(null, username)
    * match deleted == 1
    * def rows = UserOps.callGetUser(null, username)
    * match rows == '#[0]'
