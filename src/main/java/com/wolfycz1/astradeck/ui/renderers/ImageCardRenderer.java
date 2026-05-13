package com.wolfycz1.astradeck.ui.renderers;

import com.formdev.flatlaf.FlatClientProperties;
import com.wolfycz1.astradeck.model.ImageCard;
import com.wolfycz1.astradeck.model.TextCard;
import com.wolfycz1.astradeck.ui.util.ImageProvider;
import com.wolfycz1.astradeck.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class ImageCardRenderer implements FlashcardRenderer<ImageCard> {
    private final ImageProvider imageProvider;

    public ImageCardRenderer(ImageProvider imageProvider) {
        this.imageProvider = imageProvider;
    }

    @Override
    public Class<ImageCard> getSupportedType() {
        return ImageCard.class;
    }

    @Override
    public JPanel createFrontView(ImageCard card) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel textLabel = new JLabel();
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "frontTextLabel");
        Optional.ofNullable(card.getFront()).map(ImageCard.ImageSide::getText).ifPresent(textLabel::setText);

        JLabel imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        Optional.ofNullable(card.getFront()).map(ImageCard.ImageSide::getImage).ifPresent(image ->
                imageLabel.setIcon(imageProvider.getIcon(image, Constants.MAX_CARD_IMAGE_WIDTH, Constants.MAX_CARD_IMAGE_HEIGHT)));

        panel.add(Box.createVerticalStrut(10));
        panel.add(textLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(imageLabel);

        return panel;
    }

    @Override
    public JPanel createBackView(ImageCard card) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.putClientProperty(FlatClientProperties.STYLE_CLASS, "backTextLabel");
        Optional.ofNullable(card.getBack()).map(TextCard.Side::getText).ifPresent(label::setText);
        panel.add(label);

        return panel;
    }
}
