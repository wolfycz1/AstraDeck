package com.wolfycz1.astradeck.event;

import com.wolfycz1.astradeck.model.Deck;

/**
 * Signals a request for exporting a deck
 * @param deck the {@link Deck} to be exported
 * @author wolfycz1
 */
public record RequestExportEvent(Deck deck) {}
