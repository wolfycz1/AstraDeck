package com.wolfycz1.astradeck.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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

    @JsonProperty("cards")
    public List<Flashcard> getCards() {
        return new ArrayList<>(cardMap.values());
    }

    @JsonProperty("cards")
    public void setCards(List<Flashcard> cardList) {
        if (cardList != null) {
            this.cardMap = cardList.stream().collect(Collectors.toMap(Flashcard::getId, card -> card));
        } else {
            this.cardMap = new HashMap<>();
        }
    }
}
