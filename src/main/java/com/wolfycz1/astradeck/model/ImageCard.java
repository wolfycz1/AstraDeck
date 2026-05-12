package com.wolfycz1.astradeck.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImageCard extends Flashcard {
    private ImageSide front;
    private TextCard.Side back;

    @Data
    public static class ImageSide {
        private String text;
        private Media image;
    }

    @Override
    public String getPreviewText() {
        if (front != null) {
            if (front.getText() != null && !front.getText().isBlank()) {
                return front.getText();
            } else if (front.getImage() != null) {
                return "[" + front.getImage().originalName() + "]";
            }
        }
        return "[Empty Image Card]";
    }
}
