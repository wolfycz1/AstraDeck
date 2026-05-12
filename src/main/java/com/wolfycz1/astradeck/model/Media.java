package com.wolfycz1.astradeck.model;

public record Media(String name, String type, String format, String originalName) {
    public Media {
        if (originalName == null) {
            originalName = "untitled." + format;
        }
    }

    public String getPath() {
        return type + "/" + name + "." + format;
    }

    public String getFileName() {
        return name + "." + format;
    }
}
