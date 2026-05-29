package com.wolfycz1.astradeck.event;

import com.wolfycz1.astradeck.model.Deck;

/**
 * Signals a request for a study panel for a selected deck
 * @param deck selected {@link Deck} to study
 * @author wolfycz1
 */
public record RequestStudyEvent(Deck deck) {}
