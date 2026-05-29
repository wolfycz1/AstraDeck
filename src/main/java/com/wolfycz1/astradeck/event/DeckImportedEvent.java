package com.wolfycz1.astradeck.event;

import com.wolfycz1.astradeck.model.Deck;

/**
 * Signals that a deck has been imported
 * @param deck the imported {@link Deck} object
 * @author wolfycz1
 */
public record DeckImportedEvent(Deck deck) {}
