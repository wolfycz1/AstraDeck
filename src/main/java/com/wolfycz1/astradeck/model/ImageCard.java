package com.wolfycz1.astradeck.model;

import com.wolfycz1.astradeck.model.sides.ImageSide;
import com.wolfycz1.astradeck.model.sides.TextSide;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.stream.Stream;

/**
 * A flashcard with an image on the front
 * @author wolfycz1
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImageCard extends Flashcard {
    private ImageSide front;
    private TextSide back;

    /**
     * Generates a short preview text representing the image card for the UI
     * @return short preview string
     */
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

    /**
     * Collects any image files attached to this card
     * @return stream of {@link Media} objects
     */
    @Override
    public Stream<Media> getReferencedMedia() {
        if (front != null && front.getImage() != null) {
            return Stream.of(front.getImage());
        }
        return Stream.empty();
    }
}
