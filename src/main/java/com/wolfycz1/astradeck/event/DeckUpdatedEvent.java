package com.wolfycz1.astradeck.event;

import com.wolfycz1.astradeck.model.Deck;

/**
 * Signals that a deck has been updated
 * @param deck the updated {@link Deck} object
 * @author wolfycz1
 */
public record DeckUpdatedEvent(Deck deck) {}
