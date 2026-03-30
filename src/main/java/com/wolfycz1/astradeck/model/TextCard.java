package com.wolfycz1.astradeck.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TextCard extends Flashcard {
    private Side front;
    private Side back;

    @Data
    public static class Side {
        private String text;
    }
}
