package com.wolfycz1.astradeck.algorithm;

import com.wolfycz1.astradeck.model.ReviewState;

public interface ReviewAlgorithm {
    void processReview(ReviewState reviewState, ReviewGrade reviewGrade);
}
