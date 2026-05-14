package com.wolfycz1.astradeck.model;

import com.wolfycz1.astradeck.model.sides.TextSide;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TextCard extends Flashcard {
    private TextSide front;
    private TextSide back;

    @Override
    public String getPreviewText() {
        if (front != null && front.getText() != null && !front.getText().isBlank()) {
            return front.getText();
        }
        return "[Empty Text Card]";
    }
}
