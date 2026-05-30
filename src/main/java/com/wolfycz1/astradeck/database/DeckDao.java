package com.wolfycz1.astradeck.database;

import com.google.errorprone.annotations.MustBeClosed;
import com.wolfycz1.astradeck.model.Deck;
import com.wolfycz1.astradeck.model.Flashcard;
import com.wolfycz1.astradeck.model.ReviewState;
import org.jdbi.v3.json.Json;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * DAO for executing SQL operations with the deck data
 * @author wolfycz1
 */
public interface DeckDao {
    /**
     * Upserts a deck to the database
     * Upsert = UPDATE or INSERT
     * @param id id of the deck
     * @param title title of the deck
     * @param author author of the deck
     * @param description description of the deck
     * @param languages languages of the deck, serialized by JSON
     * @param createdAt time of the deck's creation
     * @param updatedAt time of the deck's last update
     */
    @SqlUpdate("""
            INSERT INTO DECKS (id, title, author, description, languages, created_at, updated_at)
            VALUES (:id, :title, :author, :description, :languages, :createdAt, :updatedAt)
            ON CONFLICT(id) DO UPDATE SET
                title = excluded.title,
                author = excluded.author,
                description = excluded.description,
                languages = excluded.languages,
                updated_at = excluded.updated_at
            """)
    void upsertDeck(
            @Bind("id") UUID id,
            @Bind("title") String title,
            @Bind("author") String author,
            @Bind("description") String description,
            @Bind("languages") @Json List<String> languages,
            @Bind("createdAt") Instant createdAt,
            @Bind("updatedAt") Instant updatedAt
    );

    /**
     * Helper method to extract flashcard data before upserting it to the database
     * @param deckId id of the deck the flashcard belongs to
     * @param card the flashcard object
     */
    default void upsertFlashcard(UUID deckId, Flashcard card) {
        _upsertFlashcard(
                card.getId(),
                deckId,
                card.getCreatedAt(),
                card.getUpdatedAt(),
                card
        );
    }

    /**
     * Upserts a flashcard to the database
     * Upsert = UPDATE or INSERT
     * @param id id of the flashcard
     * @param deckId id of the deck the flashcard belongs to
     * @param createdAt time of the flashcard's creation
     * @param updatedAt time of the flashcard's last update
     * @param data the flashcard object
     */
    @SqlUpdate("""
            INSERT INTO flashcards (id, deck_id, created_at, updated_at, data)
            VALUES (:id, :deckId, :createdAt, :updatedAt, :data)
            ON CONFLICT(id) DO UPDATE SET
                updated_at = excluded.updated_at,
                data = excluded.data
            """)
    void _upsertFlashcard(
            @Bind("id") UUID id,
            @Bind("deckId") UUID deckId,
            @Bind("createdAt") Instant createdAt,
            @Bind("updatedAt") Instant updatedAt,
            @Bind("data") @Json Flashcard data
    );

    /**
     * Upserts a review state to the database
     * Upsert = UPDATE or INSERT
     * @param reviewState review state to upsert
     */
    @SqlUpdate("""
            INSERT INTO review_states (card_id, last_review_date, next_review_date, repetitions, interval, ease_factor)
                    VALUES (:cardId, :lastReviewDate, :nextReviewDate, :repetitions, :interval, :easeFactor)
                    ON CONFLICT(card_id) DO UPDATE SET
                            last_review_date = excluded.last_review_date,
                            next_review_date = excluded.next_review_date,
                            repetitions = excluded.repetitions,
                            interval = excluded.interval,
                            ease_factor = excluded.ease_factor
            """)
    void upsertReviewState(@BindBean ReviewState reviewState);

    /**
     * Queries the database for a list of all flashcards of a deck
     * @param deckId id of the deck being queried
     * @return list of flashcard objects
     */
    @SqlQuery("SELECT data FROM flashcards WHERE deck_id = :deckId")
    @Json
    List<Flashcard> getFlashcardsForDeck(@Bind("deckId") UUID deckId);

    /**
     * Queries the database for a list of all review states of a deck
     * @param deckId id of the deck being queried
     * @return list of review state objects
     */
    @SqlQuery("""
            SELECT rs.* FROM review_states rs
            JOIN flashcards f on f.id = rs.card_id
            WHERE f.deck_id = :deckId
            """)
    @UseRowMapper(ReviewStateMapper.class)
    List<ReviewState> getReviewStatesForDeck(@Bind("deckId") UUID deckId);

    /**
     * Queries the database for a list of all decks
     * @return list of deck objects
     */
    @SqlQuery("SELECT * FROM decks")
    @UseRowMapper(DeckMapper.class)
    List<Deck> getAllDecks();

    /**
     * Requests a stream of all flashcards from the database
     * Must be closed. (Use: try-with-resources)
     * @return stream of flashcard objects
     */
    @SqlQuery("SELECT data FROM flashcards")
    @Json
    @MustBeClosed
    Stream<Flashcard> streamAllFlashcards();

    /**
     * Deletes a flashcard from the database
     * @param cardId id of the flashcard to delete
     */
    @SqlUpdate("DELETE FROM flashcards WHERE id = :cardId")
    void deleteFlashcard(@Bind("cardId") UUID cardId);

    /**
     * Deletes a deck from the database
     * @implNote decks cascade, so deleting flashcards separately isn't required
     * @param deckId id of the deck to delete
     */
    @SqlUpdate("DELETE FROM decks WHERE id = :deckId")
    void deleteDeck(@Bind("deckId") UUID deckId);

    /**
     * Resets all review states of a deck in the database
     * @param deckId id of the deck to reset all review states for
     * @param defaultReviewState the default review state object
     */
    @SqlUpdate("""
            UPDATE review_states
            SET last_review_date = :lastReviewDate,
                next_review_date = :nextReviewDate,
                repetitions = :repetitions,
                interval = :interval,
                ease_factor = :easeFactor
            WHERE card_id IN (
                SELECT id FROM flashcards WHERE deck_id = :deckId
            )
            """)
    void resetAllProgressForDeck(@Bind("deckId") UUID deckId, @BindBean ReviewState defaultReviewState);
}