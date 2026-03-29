package com.wolfycz1.astradeck.model;

import lombok.Data;

import java.time.Instant;
import java.util.*;

@Data
public class Deck {
    private UUID id = UUID.randomUUID();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    private String title;
    private String author;
    private String description;
    private List<String> languages = new ArrayList<>();

    private List<Flashcard> cards = new ArrayList<>();
    private Map<UUID, ReviewState> reviewData = new HashMap<>();
}
