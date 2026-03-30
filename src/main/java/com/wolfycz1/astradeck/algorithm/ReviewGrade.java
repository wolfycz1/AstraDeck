package com.wolfycz1.astradeck.algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReviewGrade {
    BLACKOUT(0),
    FAILED(1),
    INCORRECT(2),
    HARD(3),
    GOOD(4),
    EASY(5);

    private final int value;
}
