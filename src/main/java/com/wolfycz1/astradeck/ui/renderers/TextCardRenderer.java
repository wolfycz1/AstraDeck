package com.wolfycz1.astradeck.ui.renderers;

import com.formdev.flatlaf.FlatClientProperties;
import com.wolfycz1.astradeck.model.TextCard;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class TextCardRenderer implements FlashcardRenderer<TextCard> {
    @Override
    public Class<TextCard> getSupportedType() {
        return TextCard.class;
    }

    @Override
    public JPanel createFrontView(TextCard card) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        JLabel label = new JLabel();
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.putClientProperty(FlatClientProperties.STYLE_CLASS, "frontTextLabel");
        Optional.ofNullable(card.getFront()).map(TextCard.Side::getText).ifPresent(label::setText);
        panel.add(label);

        return panel;
    }

    @Override
    public JPanel createBackView(TextCard card) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        JLabel label = new JLabel();
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.putClientProperty(FlatClientProperties.STYLE_CLASS, "backTextLabel");
        Optional.ofNullable(card.getBack()).map(TextCard.Side::getText).ifPresent(label::setText);
        panel.add(label);

        return panel;
    }
}
