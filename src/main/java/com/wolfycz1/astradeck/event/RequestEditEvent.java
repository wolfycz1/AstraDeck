package com.wolfycz1.astradeck.event;

import com.wolfycz1.astradeck.model.Deck;

/**
 * Signals a request for the editor of a specific deck
 * @param deck the {@link Deck} to create an editor for
 * @author wolfycz1
 */
public record RequestEditEvent(Deck deck) {}
