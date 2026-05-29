package com.wolfycz1.astradeck.event;

import com.wolfycz1.astradeck.model.Flashcard;

/**
 * Signals that a new card has been presented to the user
 * @param card the card being presented
 * @param remainingCards amount of cards to be yet presented
 * @author wolfycz1
 */
public record NewCardPresentedEvent(Flashcard card, int remainingCards) {}

