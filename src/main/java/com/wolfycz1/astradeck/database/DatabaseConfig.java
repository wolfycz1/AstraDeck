package com.wolfycz1.astradeck.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wolfycz1.astradeck.util.OsPaths;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Config;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.IOException;
import java.nio.file.Files;

public class DatabaseConfig {
    private static final String DB_FILENAME = "astradeck.db";
    private static final String DB_PARAMS = "?foreign_keys=on&busy_timeout=5000";
    private static final String DB_URL = "jdbc:sqlite:" + OsPaths.DATA_DIR.resolve(DB_FILENAME).toAbsolutePath() + DB_PARAMS;

    public static Jdbi initializeDatabase() {
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

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        jdbi.getConfig(Jackson2Config.class).setMapper(mapper);

        return jdbi;
    }
}
