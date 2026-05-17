package com.wolfycz1.astradeck.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Media(String name, String type, String format, String originalName) {
    public Media {
        if (originalName == null) {
            originalName = "untitled." + format;
        }
    }

    @JsonIgnore
    public String getPath() {
        return type + "/" + name + "." + format;
    }

    @JsonIgnore
    public String getFileName() {
        return name + "." + format;
    }
}
