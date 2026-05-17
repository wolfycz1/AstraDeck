package com.wolfycz1.astradeck.event;

import com.wolfycz1.astradeck.model.Flashcard;

import java.util.UUID;

public record FlashcardUpdatedEvent(UUID deckId, Flashcard card) {}
