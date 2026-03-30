package com.wolfycz1.astradeck.event;

import com.wolfycz1.astradeck.model.Flashcard;

public record NewCardPresentedEvent(Flashcard card, int remainingCards) {}

