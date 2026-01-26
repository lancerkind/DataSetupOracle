-- Procedures and function

-- INSERT_USER: inserts a user, raises unique constraint if username exists
CREATE OR REPLACE PROCEDURE INSERT_USER(
    p_username   IN  VARCHAR2,
    p_age        IN  NUMBER,
    p_primaryKey OUT NUMBER
) AS
BEGIN
    INSERT INTO USERS (ID, USERNAME, AGE)
    VALUES (USER_ID_SEQ.NEXTVAL, p_username, p_age)
    RETURNING ID INTO p_primaryKey;
END;
/

-- GET_USER: returns a SYS_REFCURSOR for the user
CREATE OR REPLACE FUNCTION GET_USER(
    p_username IN VARCHAR2
) RETURN SYS_REFCURSOR AS
    rc SYS_REFCURSOR;
BEGIN
    OPEN rc FOR
        SELECT ID, USERNAME, AGE
        FROM USERS
        WHERE USERNAME = p_username;
    RETURN rc;
END;
/

-- DELETE_USER: deletes by exact USERNAME and returns affected rows
CREATE OR REPLACE PROCEDURE DELETE_USER(
    p_username     IN  VARCHAR2,
    p_rows_deleted OUT NUMBER
) AS
BEGIN
    DELETE FROM USERS WHERE USERNAME = p_username;
    p_rows_deleted := SQL%ROWCOUNT;
END;
/
