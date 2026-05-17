package com.wolfycz1.astradeck.event;

import java.util.UUID;

public record FlashcardDeletedEvent(UUID deckId, UUID cardId) {}
