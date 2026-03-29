package com.wolfycz1.astradeck.model;

import com.wolfycz1.astradeck.util.Constants;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Manifest {
    private UUID deckId;
    private int version = Constants.ASTRA_FORMAT_VERSION;
    private String minAppVersion = Constants.MAJOR_APP_VERSION;
    private String createdWithVersion = Constants.APP_VERSION;

    private List<Media> mediaList = new ArrayList<>();

    private String title;
    private String author;
    private String description;
    private List<String> languages = new ArrayList<>();
    private int totalCards;
    private Instant earliestDueDate;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
}
