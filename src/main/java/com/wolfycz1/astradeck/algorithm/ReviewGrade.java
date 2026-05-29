package com.wolfycz1.astradeck.algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the quality of the user's recall during a review
 * @author wolfycz1
 */
@AllArgsConstructor
@Getter
public enum ReviewGrade {
    BLACKOUT(0),
    FAILED(1),
    INCORRECT(2),
    HARD(3),
    GOOD(4),
    EASY(5);

    /**
     * The raw numerical value of a grade
     */
    private final int value;
}
