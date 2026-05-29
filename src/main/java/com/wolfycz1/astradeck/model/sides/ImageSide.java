package com.wolfycz1.astradeck.model.sides;

import com.wolfycz1.astradeck.model.Media;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a side of a flashcard containing an image alongside text
 * @author wolfycz1
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImageSide extends TextSide {
    private Media image;
}
