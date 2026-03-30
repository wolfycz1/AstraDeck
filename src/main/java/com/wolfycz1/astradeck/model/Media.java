package com.wolfycz1.astradeck.model;

public record Media(String name, String type, String format) {
    public String getPath() {
        return type + "/" + name + "." + format;
    }

    public String getFileName() {
        return name + "." + format;
    }
}
