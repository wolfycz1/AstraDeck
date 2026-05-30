package com.wolfycz1.astradeck.ui.renderers;

import com.formdev.flatlaf.FlatClientProperties;
import com.wolfycz1.astradeck.model.TextCard;
import com.wolfycz1.astradeck.model.sides.TextSide;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * Generates the visual UI panel for a text-only flashcard
 * @author wolfycz1
 */
public class TextCardRenderer implements FlashcardRenderer<TextCard> {
    /**
     * Returns the class type this renderer supports
     * @return {@link TextCard} class
     */
    @Override
    public Class<TextCard> getSupportedType() {
        return TextCard.class;
    }

    /**
     * Builds the UI panel for the front side of the flashcard
     * @param card card to build the ui for
     * @return the {@link JPanel} for the front view
     */
    @Override
    public JPanel createFrontView(TextCard card) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        JLabel label = new JLabel();
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.putClientProperty(FlatClientProperties.STYLE, "font: +8");
        Optional.ofNullable(card.getFront())
                .map(TextSide::getText)
                .map(text -> "<html><center>" +
                        text.replace("\n", "<br>") + "</center></html>")
                .ifPresent(label::setText);
        panel.add(label);

        return panel;
    }

    /**
     * Builds the UI panel for the back side of the flashcard
     * @param card card to build the ui for
     * @return the {@link JPanel} for the back view
     */
    @Override
    public JPanel createBackView(TextCard card) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        JLabel label = new JLabel();
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.putClientProperty(FlatClientProperties.STYLE, "font: +4");
        Optional.ofNullable(card.getBack())
                .map(TextSide::getText)
                .map(text -> "<html><center>" +
                        text.replace("\n", "<br>") + "</center></html>")
                .ifPresent(label::setText);
        panel.add(label);

        return panel;
    }
}
