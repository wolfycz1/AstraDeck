package com.wolfycz1.astradeck.event;

import com.wolfycz1.astradeck.model.Flashcard;

import java.util.UUID;

/**
 * Signals that a flashcard has been updated
 * @param deckId id of the deck which's flashcard has been updated
 * @param card the updated {@link Flashcard} object
 * @author wolfycz1
 */
public record FlashcardUpdatedEvent(UUID deckId, Flashcard card) {}
