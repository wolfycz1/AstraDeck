package com.wolfycz1.astradeck.event;

import com.wolfycz1.astradeck.model.ReviewState;

import java.util.UUID;

public record DeckResetEvent(UUID deckId, ReviewState defaultReviewState) {
}
