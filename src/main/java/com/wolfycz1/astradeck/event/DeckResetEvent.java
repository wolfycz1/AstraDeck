package com.wolfycz1.astradeck.event;

import com.wolfycz1.astradeck.model.ReviewState;

import java.util.UUID;

/**
 * Signals that all review progress for a deck has been reset
 * @param deckId id of the deck being reset
 * @param defaultReviewState default {@link ReviewState} object
 * @author wolfycz1
 */
public record DeckResetEvent(UUID deckId, ReviewState defaultReviewState) {}
