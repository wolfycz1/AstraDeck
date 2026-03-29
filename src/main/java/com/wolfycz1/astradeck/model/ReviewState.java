package com.wolfycz1.astradeck.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReviewState {
    private UUID cardId;
    private LocalDateTime lastReviewDate;
    private LocalDateTime nextReviewDate = LocalDateTime.now();
    private int repetitions = 0;
    private int interval = 0;
    private double easeFactor = 2.5;
}
