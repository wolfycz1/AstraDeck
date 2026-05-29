package com.wolfycz1.astradeck.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a media file attached to a flashcard
 * @param name hashed name of the file
 * @param type type of the media file
 * @param format file format of the media file
 * @param originalName original name of the file used for UI
 * @author wolfycz1
 */
public record Media(String name, String type, String format, String originalName) {
    /**
     * Backwards compatibility for media objects without original name
     */
    public Media {
        if (originalName == null) {
            originalName = "untitled." + format;
        }
    }

    /**
     * Builds the relative path of the media file
     * @return path as string
     */
    @JsonIgnore
    public String getPath() {
        return type + "/" + name + "." + format;
    }

    /**
     * Reconstructs full file name with extension
     * @return file name as string
     */
    @JsonIgnore
    public String getFileName() {
        return name + "." + format;
    }
}
