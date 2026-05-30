package com.wolfycz1.astradeck.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wolfycz1.astradeck.util.OsPaths;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Config;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Handles the setup and configuration of the local SQLite database
 * @author wolfycz1
 */
public class DatabaseConfig {
    private static final String DB_FILENAME = "astradeck.db";
    private static final String DB_PARAMS = "?foreign_keys=on&busy_timeout=5000";
    private static final String DB_URL = "jdbc:sqlite:" + OsPaths.DATA_DIR.resolve(DB_FILENAME).toAbsolutePath() + DB_PARAMS;

    /**
     * Creates the app's data directory
     * Runs database migration via Flyway
     * Configures Jdbi
     * @return configured Jdbi instance for database operations
     */
    public static Jdbi initializeDatabase(ObjectMapper objectMapper) {
        try {
            Files.createDirectories(OsPaths.DATA_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data directory at " + OsPaths.DATA_DIR, e);
        }

        Flyway flyway = Flyway.configure().dataSource(DB_URL, "", "").load();
        flyway.migrate();

        Jdbi jdbi = Jdbi.create(DB_URL);
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new Jackson2Plugin());

        jdbi.getConfig(Jackson2Config.class).setMapper(objectMapper);

        return jdbi;
    }
}
