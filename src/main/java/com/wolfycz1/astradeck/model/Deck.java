package com.wolfycz1.astradeck.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a collection of flashcards and their review states
 * @author wolfycz1
 */
@Data
public class Deck {
    private UUID id = UUID.randomUUID();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    private String title;
    private String author;
    private String description;
    private List<String> languages = new ArrayList<>();

    @JsonIgnore
    private Map<UUID, Flashcard> cardMap = new HashMap<>();

    private Map<UUID, ReviewState> reviewData = new HashMap<>();

    /**
     * Converts card map to a list for JSON serialization
     * @return list of flashcards
     */
    @JsonProperty("cards")
    public List<Flashcard> getCards() {
        return new ArrayList<>(cardMap.values());
    }

    /**
     * Rebuilds the card map from a deserialized JSON card list
     * @param cardList deserialized list of flashcards
     */
    @JsonProperty("cards")
    public void setCards(List<Flashcard> cardList) {
        if (cardList != null) {
            this.cardMap = cardList.stream().collect(Collectors.toMap(Flashcard::getId, card -> card));
        } else {
            this.cardMap = new HashMap<>();
        }
    }

    /**
     * Adds a new flashcard and its review state
     * @param card new {@link Flashcard} object
     * @param reviewState its {@link ReviewState} object
     */
    public void addCard(Flashcard card, ReviewState reviewState) {
        this.cardMap.put(card.getId(), card);
        this.reviewData.put(card.getId(), reviewState);
        this.updatedAt = Instant.now();
    }

    /**
     * Removes a flashcard from the deck by its card id
     * @param cardId id of the card to remove
     */
    public void removeCard(UUID cardId) {
        this.cardMap.remove(cardId);
        this.reviewData.remove(cardId);
        this.updatedAt = Instant.now();
    }

    /**
     * Marks the deck and flashcard as recently modified
     * @param card the updated card
     */
    public void updateCardContent(Flashcard card) {
        card.setUpdatedAt(Instant.now());
        this.updatedAt = Instant.now();
    }

    /**
     * Resets all review states of the deck
     */
    public void resetDeckProgress() {
        this.reviewData.clear();
        for (Flashcard card : cardMap.values()) {
            ReviewState reviewState = new ReviewState();
            reviewState.setCardId(card.getId());
            this.reviewData.put(card.getId(), reviewState);
        }
    }
}
