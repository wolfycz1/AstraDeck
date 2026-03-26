package com.wolfycz1.astradeck.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImageCard extends Flashcard {
    private String frontText;
    private String imageFileName;
    private String backText;
}
