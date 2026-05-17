package com.wolfycz1.astradeck.database;

import java.sql.Timestamp;
import java.time.Instant;

public class MapperUtil {
    public static Instant parseInstant(Object value) {
        return switch (value) {
            case Number number -> Instant.ofEpochMilli(number.longValue());
            case String string -> Instant.parse(string);
            case Timestamp timestamp -> timestamp.toInstant();
            case null, default -> null;
        };
    }
}
