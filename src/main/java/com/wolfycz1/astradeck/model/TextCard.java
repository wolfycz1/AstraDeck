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

    @Override
    public String getPreviewText() {
        if (front != null && front.getText() != null && !front.getText().isBlank()) {
            return front.getText();
        }
        return "[Empty Text Card]";
    }
}
