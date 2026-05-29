package com.wolfycz1.astradeck.database;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Utility class for common database mapping operations
 * @author wolfycz1
 */
public class MapperUtil {
    /**
     * Converts different database time formats to an {@link Instant}
     * @param value raw object from the database (Number, String, Timestamp)
     * @return parsed {@link Instant}, or null if {@code value} is missing or unrecognized
     */
    public static Instant parseInstant(Object value) {
        return switch (value) {
            case Number number -> Instant.ofEpochMilli(number.longValue());
            case String string -> Instant.parse(string);
            case Timestamp timestamp -> timestamp.toInstant();
            case null, default -> null;
        };
    }
}
