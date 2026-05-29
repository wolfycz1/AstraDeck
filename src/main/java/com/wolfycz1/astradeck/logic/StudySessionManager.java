package com.wolfycz1.astradeck.logic;

import com.google.common.eventbus.EventBus;
import com.wolfycz1.astradeck.algorithm.ReviewAlgorithm;
import com.wolfycz1.astradeck.algorithm.ReviewGrade;
import com.wolfycz1.astradeck.event.NewCardPresentedEvent;
import com.wolfycz1.astradeck.event.ReviewStateUpdatedEvent;
import com.wolfycz1.astradeck.event.SessionAbortedEvent;
import com.wolfycz1.astradeck.event.SessionFinishedEvent;
import com.wolfycz1.astradeck.model.Deck;
import com.wolfycz1.astradeck.model.Flashcard;
import com.wolfycz1.astradeck.model.ReviewState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Controls the study session, queues due cards and tracks progress
 * @author wolfycz1
 */
@RequiredArgsConstructor
public class StudySessionManager {
    private final Deck deck;
    private final ReviewAlgorithm algorithm;
    private final EventBus eventBus;

    private final PriorityQueue<ReviewState> dueCardsQueue = new PriorityQueue<>(
            Comparator.comparing(ReviewState::getNextReviewDate, Comparator.nullsFirst(Comparator.naturalOrder()))
    );

    @Getter
    private Flashcard currentCard;
    @Getter
    private ReviewState currentState;
    @Getter
    private int totalCardsDue;
    @Getter
    private int cardsReviewedThisSession = 0;

    /**
     * Initializes the session by finding all cards due for review and loads the first one
     */
    public void startSession() {
        Instant now = Instant.now();
        resetSession();

        for (ReviewState state : deck.getReviewData().values()) {
            if (state.getNextReviewDate() == null || state.getNextReviewDate().isBefore(now)) {
                dueCardsQueue.add(state);
            }
        }

        totalCardsDue = dueCardsQueue.size();

        if (dueCardsQueue.isEmpty()) {
            eventBus.post(new SessionFinishedEvent(totalCardsDue));
            return;
        }

        loadNextCard();
    }

    /**
     * Loads the next due card from the queue and presents it
     */
    private void loadNextCard() {
        currentState = dueCardsQueue.poll();
        if (currentState != null) {
            currentCard = deck.getCardMap().get(currentState.getCardId());
            if (currentCard == null) {
                throw new IllegalStateException("Card content missing for state.");
            }
            eventBus.post(new NewCardPresentedEvent(currentCard, getRemainingCardsCount()));
        } else {
            eventBus.post(new SessionFinishedEvent(cardsReviewedThisSession));
            resetSession();
        }
    }

    /**
     * Updates the card's review state based on the grade selected and triggers a new card load
     * @param grade the grade given
     */
    public void processAnswer(ReviewGrade grade) {
        if (currentState == null) return;

        algorithm.processReview(currentState, grade);
        cardsReviewedThisSession++;

        eventBus.post(new ReviewStateUpdatedEvent(currentState));

        if (!currentState.getNextReviewDate().isAfter(Instant.now())) {
            dueCardsQueue.add(currentState);
        }

        loadNextCard();
    }

    /**
     * Aborts the current study session
     */
    public void abortSession() {
        resetSession();
        eventBus.post(new SessionAbortedEvent());
    }

    /**
     * Returns the title of the currently studied deck
     * @return title of the deck
     */
    public String getDeckTitle() {
        return deck.getTitle();
    }

    /**
     * Calculates remaining card count in this session
     * @return number of cards remaining
     */
    public int getRemainingCardsCount() {
        return dueCardsQueue.size() + (currentState != null ? 1 : 0);
    }

    /**
     * Clears the queue and resets all progress counters
     */
    private void resetSession() {
        dueCardsQueue.clear();
        currentCard = null;
        currentState = null;
        totalCardsDue = 0;
        cardsReviewedThisSession = 0;
    }
}
