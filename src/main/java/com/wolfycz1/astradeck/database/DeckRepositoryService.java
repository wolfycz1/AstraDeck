package com.wolfycz1.astradeck.database;

import com.google.common.eventbus.Subscribe;
import com.wolfycz1.astradeck.event.*;
import com.wolfycz1.astradeck.model.*;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service class handling database transactions
 * @author wolfycz1
 */
@RequiredArgsConstructor
public class DeckRepositoryService {
    private final Jdbi jdbi;

    /**
     * Loads all decks from the database, including flashcards and review states
     * @return list of all the deck objects
     */
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

    /**
     * Scans for all references to media files.
     * @return set of all media files that have at least one reference
     */
    public Set<String> getAllReferencedMediaHashes() {
        return jdbi.withExtension(DeckDao.class, deckDao -> {
            try (Stream<Flashcard> stream = deckDao.streamAllFlashcards()) {
                return stream.flatMap(Flashcard::getReferencedMedia)
                        .map(Media::name)
                        .collect(Collectors.toSet());
            }
        });
    }

    /**
     * Saves an imported deck and its contents to the database
     * @param event event containing the imported deck
     */
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

    /**
     * Updates existing deck's metadata to the database
     * @param event event containing the updated deck
     */
    @Subscribe
    public void onDeckUpdated(DeckUpdatedEvent event) {
        Deck deck = event.deck();
        jdbi.useExtension(DeckDao.class, deckDao -> deckDao.upsertDeck(deck.getId(), deck.getTitle(),
                deck.getAuthor(), deck.getDescription(), deck.getLanguages(), deck.getCreatedAt(), deck.getUpdatedAt()));
    }

    /**
     * Updates changes of a flashcard to the database
     * @param event event containing the updated flashcard
     */
    @Subscribe
    public void onFlashcardUpdated(FlashcardUpdatedEvent event) {
        jdbi.useExtension(DeckDao.class, deckDao -> deckDao.upsertFlashcard(event.deckId(), event.card()));
    }

    /**
     * Deletes a flashcard from the database
     * @param event event containing the flashcard to be deleted
     */
    @Subscribe
    public void onFlashcardDeleted(FlashcardDeletedEvent event) {
        jdbi.useExtension(DeckDao.class, deckDao -> deckDao.deleteFlashcard(event.cardId()));
    }

    /**
     * Deletes a deck from the database
     * @param event event containing the deck to be deleted
     */
    @Subscribe
    public void onDeckDeleted(DeckDeletedEvent event) {
        jdbi.useExtension(DeckDao.class, deckDao -> deckDao.deleteDeck(event.deckId()));
    }

    /**
     * Resets the review states of a deck
     * @param event event containing the deck to be reset
     */
    @Subscribe
    public void onDeckReset(DeckResetEvent event) {
        jdbi.useExtension(DeckDao.class, deckDao -> deckDao.resetAllProgressForDeck(event.deckId(), event.defaultReviewState()));
    }

    /**
     * Updates a review state to the database
     * @param event event containing the review state to be updated
     */
    @Subscribe
    public void onReviewStateUpdated(ReviewStateUpdatedEvent event) {
        jdbi.useExtension(DeckDao.class, deckDao -> deckDao.upsertReviewState(event.reviewState()));
    }
}
