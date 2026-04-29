package com.wolfycz1.astradeck.logic;

import com.wolfycz1.astradeck.model.Deck;
import com.wolfycz1.astradeck.model.Flashcard;
import com.wolfycz1.astradeck.model.ReviewState;

import java.time.Instant;
import java.util.UUID;

public class DeckEditorService {
    public static void addCard(Deck deck, Flashcard card) {
        deck.getCardMap().put(card.getId(), card);

        ReviewState state = new ReviewState();
        state.setCardId(card.getId());

        deck.getReviewData().put(card.getId(), state);
        deck.setUpdatedAt(Instant.now());
    }

    public static void removeCard(Deck deck, UUID cardId) {
        deck.getCardMap().remove(cardId);
        deck.getReviewData().remove(cardId);
        deck.setUpdatedAt(Instant.now());
    }

    public static void updateCardContent(Deck deck, Flashcard card) {
        card.setUpdatedAt(Instant.now());
        deck.setUpdatedAt(Instant.now());
    }

    public static void resetDeckProgress(Deck deck) {
        deck.getReviewData().clear();

        for (Flashcard card : deck.getCardMap().values()) {
            ReviewState newState = new ReviewState();
            newState.setCardId(card.getId());
            deck.getReviewData().put(card.getId(), newState);
        }

        deck.setUpdatedAt(Instant.now());
    }
}
