package com.wolfycz1.astradeck.algorithm;

import com.wolfycz1.astradeck.model.ReviewState;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Implements the SM-2 space repetition alogorithm to calculate review intervals
 * @author woflycz1
 */
public class Sm2Algorithm implements ReviewAlgorithm {
    /**
     * Calculates the next review date, interval, ease-factor based on the SM-2 formula.
     * Modifies review state directly
     * @param state current state of the item being review
     * @param grade the grade given
     */
    @Override
    public void processReview(ReviewState state, ReviewGrade grade) {
        int q = grade.getValue();
        state.setLastReviewDate(Instant.now());

        if (q < 3) {
            state.setRepetitions(0);
            state.setInterval(0);
        } else {
            int currentReps = state.getRepetitions();

            switch (currentReps) {
                case 0 -> state.setInterval(1);
                case 1 -> state.setInterval(6);
                default -> {
                    int nextInterval = (int) Math.round(state.getInterval() * state.getEaseFactor());
                    state.setInterval(nextInterval);
                }
            }

            state.setRepetitions(currentReps + 1);

            double newEase = state.getEaseFactor() + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02));

            if (newEase < 1.3) {
                newEase = 1.3;
            }
            state.setEaseFactor(newEase);
        }

        Instant nextDue = Instant.now().plus(state.getInterval(), ChronoUnit.DAYS);
        state.setNextReviewDate(nextDue);
    }
}
