package com.wolfycz1.astradeck.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks spaced repetition progress for a single flashcard
 * @author wolfycz1
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReviewState {
    @EqualsAndHashCode.Include
    private UUID cardId;
    private Instant lastReviewDate;
    private Instant nextReviewDate = Instant.now();
    private int repetitions = 0;
    private int interval = 0;
    private double easeFactor = 2.5;
}
