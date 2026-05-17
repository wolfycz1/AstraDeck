package com.wolfycz1.astradeck.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Config;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class DatabaseConfig {
    // ~ change db path later
    private static final String DB_URL = "jdbc:sqlite:astradeck.db?foreign_keys=on&busy_timeout=5000";

    public static Jdbi initializeDatabase() {
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
