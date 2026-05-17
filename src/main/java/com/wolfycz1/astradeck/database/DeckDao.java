package com.wolfycz1.astradeck.database;

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

public interface DeckDao {
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

    default void upsertFlashcard(UUID deckId, Flashcard card) {
        _upsertFlashcard(
                card.getId(),
                deckId,
                card.getCreatedAt(),
                card.getUpdatedAt(),
                card
        );
    }

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

    @SqlQuery("SELECT data FROM flashcards WHERE deck_id = :deckId")
    @Json
    List<Flashcard> getFlashcardsForDeck(@Bind("deckId") UUID deckId);

    @SqlQuery("""
            SELECT rs.* FROM review_states rs
            JOIN flashcards f on f.id = rs.card_id
            WHERE f.deck_id = :deckId
            """)
    @UseRowMapper(ReviewStateMapper.class)
    List<ReviewState> getReviewStatesForDeck(@Bind("deckId") UUID deckId);

    @SqlQuery("SELECT * FROM decks")
    @UseRowMapper(DeckMapper.class)
    List<Deck> getAllDecks();

    @SqlQuery("SELECT data FROM flashcards")
    @Json
    Stream<Flashcard> streamAllFlashcards();

    @SqlUpdate("DELETE FROM flashcards WHERE id = :cardId")
    void deleteFlashcard(@Bind("cardId") UUID cardId);

    @SqlUpdate("DELETE FROM decks WHERE id = :deckId")
    void deleteDeck(@Bind("deckId") UUID deckId);

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