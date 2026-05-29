package com.wolfycz1.astradeck.event;

import java.util.UUID;

/**
 * Signals that a flashcard has been deleted
 * @param deckId id of the deck the flashcard has been deleted from
 * @param cardId id of the flashcard being deleted
 * @author wolfycz1
 */
public record FlashcardDeletedEvent(UUID deckId, UUID cardId) {}
