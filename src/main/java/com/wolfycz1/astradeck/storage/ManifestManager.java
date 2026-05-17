package com.wolfycz1.astradeck.storage;

import com.wolfycz1.astradeck.model.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ManifestManager {
    public static Manifest generateManifest(Deck deck) {
        Manifest manifest = new Manifest();
        manifest.setDeckId(deck.getId());
        manifest.setTitle(deck.getTitle());
        manifest.setAuthor(deck.getAuthor());
        manifest.setDescription(deck.getTitle());
        manifest.setLanguages(new ArrayList<>(deck.getLanguages()));
        manifest.setTotalCards(deck.getCards().size());
        manifest.setCreatedAt(deck.getCreatedAt());
        manifest.setUpdatedAt(Instant.now());

        Instant earliestDue = deck.getReviewData().values().stream()
                .map(ReviewState::getNextReviewDate)
                .filter(Objects::nonNull)
                .min(Instant::compareTo)
                .orElse(null);
        manifest.setEarliestDueDate(earliestDue);

        Set<Media> media = deck.getCards().stream()
                        .flatMap(Flashcard::getReferencedMedia)
                        .collect(Collectors.toSet());
        manifest.setMediaList(new ArrayList<>(media));

        return manifest;
    }
}
