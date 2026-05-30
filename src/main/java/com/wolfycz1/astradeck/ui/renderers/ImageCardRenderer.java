package com.wolfycz1.astradeck.ui.renderers;

import com.formdev.flatlaf.FlatClientProperties;
import com.wolfycz1.astradeck.model.ImageCard;
import com.wolfycz1.astradeck.model.sides.ImageSide;
import com.wolfycz1.astradeck.model.sides.TextSide;
import com.wolfycz1.astradeck.ui.util.ImageProvider;
import com.wolfycz1.astradeck.util.Constants;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * Generates the visual UI panel for a flashcard that include an image alongside text
 * @author wolfycz1
 */
@RequiredArgsConstructor
public class ImageCardRenderer implements FlashcardRenderer<ImageCard> {
    private final ImageProvider imageProvider;

    /**
     * Returns the class type this renderer supports
     * @return {@link ImageCard} class
     */
    @Override
    public Class<ImageCard> getSupportedType() {
        return ImageCard.class;
    }

    /**
     * Builds the UI panel for the front side of the flashcard
     * @param card card to build the ui for
     * @return the {@link JPanel} for the front view
     */
    @Override
    public JPanel createFrontView(ImageCard card) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel textLabel = new JLabel();
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        textLabel.putClientProperty(FlatClientProperties.STYLE, "font: +8");
        Optional.ofNullable(card.getFront())
                .map(ImageSide::getText)
                .map(this::formatCardText)
                .ifPresent(textLabel::setText);

        JLabel imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        Optional.ofNullable(card.getFront()).map(ImageSide::getImage).ifPresent(image ->
                imageLabel.setIcon(imageProvider.getIcon(image, Constants.MAX_CARD_IMAGE_WIDTH, Constants.MAX_CARD_IMAGE_HEIGHT)));

        panel.add(Box.createVerticalStrut(10));
        panel.add(textLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(imageLabel);

        return panel;
    }

    /**
     * Builds the UI panel for the back side of the flashcard
     * @param card card to build the ui for
     * @return the {@link JPanel} for the back view
     */
    @Override
    public JPanel createBackView(ImageCard card) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.putClientProperty(FlatClientProperties.STYLE, "font: +4");
        Optional.ofNullable(card.getBack())
                .map(TextSide::getText)
                .map(this::formatCardText)
                .ifPresent(label::setText);
        panel.add(label);

        return panel;
    }
}
