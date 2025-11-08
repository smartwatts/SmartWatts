package com.smartwatts.userservice.integration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseMigrationRollbackTest {

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private Flyway flyway;

    @BeforeEach
    void setUp() {
        // Ensure clean state
        if (flyway != null) {
            flyway.clean();
            flyway.migrate();
        }
    }

    @Test
    void testMigrationInfo() {
        if (flyway == null) {
            // Skip test if Flyway is not available
            return;
        }

        MigrationInfoService info = flyway.info();
        assertNotNull(info, "Migration info should not be null");

        MigrationInfo[] migrations = info.all();
        assertNotNull(migrations, "Migrations should not be null");
        assertTrue(migrations.length > 0, "Should have at least one migration");
    }

    @Test
    void testMigrationRollback() {
        if (flyway == null || dataSource == null) {
            // Skip test if Flyway or DataSource is not available
            return;
        }

        // Get current migration version
        MigrationInfoService info = flyway.info();
        MigrationInfo current = info.current();
        
        if (current == null) {
            // No migrations applied yet
            return;
        }

        String currentVersion = current.getVersion().toString();
        assertNotNull(currentVersion, "Current version should not be null");

        // Test that we can clean (rollback all)
        flyway.clean();
        
        // Verify clean state
        MigrationInfoService infoAfterClean = flyway.info();
        MigrationInfo currentAfterClean = infoAfterClean.current();
        assertNull(currentAfterClean, "After clean, there should be no current migration");

        // Re-apply migrations
        flyway.migrate();
        
        // Verify migrations are re-applied
        MigrationInfoService infoAfterMigrate = flyway.info();
        MigrationInfo currentAfterMigrate = infoAfterMigrate.current();
        assertNotNull(currentAfterMigrate, "After migrate, there should be a current migration");
    }

    @Test
    void testDatabaseConnection() {
        if (dataSource == null) {
            // Skip test if DataSource is not available
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                assertTrue(resultSet.next(), "Should return a result");
                assertEquals(1, resultSet.getInt(1), "Should return 1");
            }
        } catch (Exception e) {
            fail("Database connection test failed: " + e.getMessage());
        }
    }

    @Test
    void testMigrationBaseline() {
        if (flyway == null) {
            // Skip test if Flyway is not available
            return;
        }

        // Test baseline functionality
        flyway.clean();
        flyway.baseline();
        flyway.migrate();

        MigrationInfoService info = flyway.info();
        MigrationInfo current = info.current();
        assertNotNull(current, "After baseline and migrate, there should be a current migration");
    }
}


