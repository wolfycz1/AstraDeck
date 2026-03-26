package com.wolfycz1.astradeck.model;

import lombok.Data;

import java.util.UUID;
import java.time.LocalDateTime;

@Data
public abstract class Flashcard {
    private UUID id = UUID.randomUUID();
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime lastReviewDate;
    private LocalDateTime nextReviewDate = LocalDateTime.now();
    private int repetitions = 0;
    private int interval = 0;
    private double easeFactor = 2.5;
}
