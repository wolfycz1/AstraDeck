package com.wolfycz1.astradeck.logic;

import com.google.common.eventbus.EventBus;
import com.wolfycz1.astradeck.algorithm.ReviewAlgorithm;
import com.wolfycz1.astradeck.algorithm.ReviewGrade;
import com.wolfycz1.astradeck.event.NewCardPresentedEvent;
import com.wolfycz1.astradeck.event.SessionFinishedEvent;
import com.wolfycz1.astradeck.model.Deck;
import com.wolfycz1.astradeck.model.Flashcard;
import com.wolfycz1.astradeck.model.ReviewState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Comparator;
import java.util.PriorityQueue;

@RequiredArgsConstructor
public class StudySessionManager {
    private final Deck deck;
    private final ReviewAlgorithm algorithm;
    private final EventBus eventBus;

    private final PriorityQueue<ReviewState> dueCardsQueue = new PriorityQueue<>(
            Comparator.comparing(ReviewState::getNextReviewDate)
    );

    @Getter
    private Flashcard currentCard;
    @Getter
    private ReviewState currentState;
    @Getter
    private int totalCardsDue;
    @Getter
    private int cardsReviewedThisSession = 0;

    public void startSession() {
        Instant now = Instant.now();
        dueCardsQueue.clear();

        for (ReviewState state : deck.getReviewData().values()) {
            if (state.getNextReviewDate().isBefore(now)) {
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

    private void loadNextCard() {
        currentState = dueCardsQueue.poll();
        if (currentState != null) {
            currentCard = deck.getCards().stream()
                    .filter(c -> c.getId().equals(currentState.getCardId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Card content missing for state."));

            eventBus.post(new NewCardPresentedEvent(currentCard, getRemainingCardsCount()));
        } else {
            eventBus.post(new SessionFinishedEvent(cardsReviewedThisSession));
        }
    }

    public void processAnswer(ReviewGrade grade) {
        if (currentState == null) return;

        algorithm.processReview(currentState, grade);
        cardsReviewedThisSession++;

        if (grade.getValue() < 3) {
            currentState.setNextReviewDate(Instant.now());
            dueCardsQueue.add(currentState);
        }

        loadNextCard();
    }

    public int getRemainingCardsCount() {
        return dueCardsQueue.size() + (currentState != null ? 1 : 0);
    }
}
