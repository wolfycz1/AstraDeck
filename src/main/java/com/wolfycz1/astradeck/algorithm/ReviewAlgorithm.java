package com.wolfycz1.astradeck.algorithm;

import com.wolfycz1.astradeck.model.ReviewState;

/**
 * Defines core logic for processing a review.
 * @author wolfycz1
 */
public interface ReviewAlgorithm {
    /**
     * Updates the given review state based on the review grade provided.
     * @param reviewState current state of the item being review
     * @param reviewGrade the grade given
     */
    void processReview(ReviewState reviewState, ReviewGrade reviewGrade);
}
