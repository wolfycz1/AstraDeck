package com.wolfycz1.astradeck.database;

import com.google.common.eventbus.Subscribe;
import com.wolfycz1.astradeck.event.*;
import com.wolfycz1.astradeck.model.*;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeckRepositoryService {
    private final Jdbi jdbi;

    public DeckRepositoryService(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<Deck> loadAllDecks() {
        return jdbi.inTransaction(handle -> {
            DeckDao deckDao = handle.attach(DeckDao.class);
            List<Deck> decks = deckDao.getAllDecks();

            if (decks == null) {
                return new ArrayList<>();
            }

            for (Deck deck : decks) {
                List<Flashcard> cards = deckDao.getFlashcardsForDeck(deck.getId());
                deck.setCards(cards);

                List<ReviewState> reviewStates = deckDao.getReviewStatesForDeck(deck.getId());
                for (ReviewState state : reviewStates) {
                    deck.getReviewData().put(state.getCardId(), state);
                }
            }

            return decks;
        });
    }

    public Set<String> getAllReferencedMediaHashes() {
        return jdbi.withExtension(DeckDao.class, deckDao -> {
            try (Stream<Flashcard> stream = deckDao.streamAllFlashcards()) {
                return stream.flatMap(Flashcard::getReferencedMedia)
                        .map(Media::name)
                        .collect(Collectors.toSet());
            }
        });
    }

    @Subscribe
    public void onDeckImported(DeckImportedEvent event) {
        Deck deck = event.deck();
        jdbi.useTransaction(handle -> {
            DeckDao deckDao = handle.attach(DeckDao.class);
            deckDao.upsertDeck(deck.getId(), deck.getTitle(), deck.getAuthor(), deck.getDescription(),
                    deck.getLanguages(), deck.getCreatedAt(), deck.getUpdatedAt());

            for (Flashcard card : deck.getCardMap().values()) {
                deckDao.upsertFlashcard(deck.getId(), card);
            }

            for (ReviewState reviewState : deck.getReviewData().values()) {
                deckDao.upsertReviewState(reviewState);
            }
        });
    }

    @Subscribe
    public void onDeckUpdated(DeckUpdatedEvent event) {
        Deck deck = event.deck();
        jdbi.useExtension(DeckDao.class, deckDao -> deckDao.upsertDeck(deck.getId(), deck.getTitle(),
                deck.getAuthor(), deck.getDescription(), deck.getLanguages(), deck.getCreatedAt(), deck.getUpdatedAt()));
    }

    @Subscribe
    public void onFlashcardUpdated(FlashcardUpdatedEvent event) {
        jdbi.useExtension(DeckDao.class, deckDao -> deckDao.upsertFlashcard(event.deckId(), event.card()));
    }

    @Subscribe
    public void onFlashcardDeleted(FlashcardDeletedEvent event) {
        jdbi.useExtension(DeckDao.class, deckDao -> deckDao.deleteFlashcard(event.cardId()));
    }

    @Subscribe
    public void onDeckDeleted(DeckDeletedEvent event) {
        jdbi.useExtension(DeckDao.class, deckDao -> deckDao.deleteDeck(event.deckId()));
    }

    @Subscribe
    public void onDeckReset(DeckResetEvent event) {
        jdbi.useExtension(DeckDao.class, deckDao -> deckDao.resetAllProgressForDeck(event.deckId(), event.defaultReviewState()));
    }

    @Subscribe
    public void onReviewStateUpdated(ReviewStateUpdatedEvent event) {
        jdbi.useExtension(DeckDao.class, deckDao -> deckDao.upsertReviewState(event.reviewState()));
    }
}
