package com.wolfycz1.astradeck.event;

import java.util.UUID;

/**
 * Signals that a deck has been deleted
 * @param deckId id of the deck deleted
 * @author wolfycz1
 */
public record DeckDeletedEvent(UUID deckId) {}
