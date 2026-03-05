package biz.agilenoir;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;

public class LiquibaseMigrations {
    ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();

    public void run(String url, String changelogPath) {
        try (Connection connection = DriverManager.getConnection(url)) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase(changelogPath, resourceAccessor, database)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to run Liquibase migrations", e);
        }
    }

    public void rollbackToInitial(String url, String changelogPath) {
        try (Connection connection = DriverManager.getConnection(url)) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase(changelogPath, resourceAccessor, database)) {
                liquibase.rollback("initial", new Contexts(), new LabelExpression());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to rollback Liquibase migrations to initial", e);
        }
    }

    public void rollback(String url, String changelogPath, int count) {
        try (Connection connection = DriverManager.getConnection(url)) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase(changelogPath, resourceAccessor, database)) {
                liquibase.rollback(count, new Contexts(), new LabelExpression());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to rollback Liquibase migrations by count: " + count, e);
        }
    }
}
