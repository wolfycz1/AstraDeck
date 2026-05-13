package com.wolfycz1.astradeck.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.wolfycz1.astradeck.model.Flashcard;
import com.wolfycz1.astradeck.ui.renderers.FlashcardRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public class CardViewPanel extends JPanel {
    private final JPanel frontContainer;
    private final JSeparator separator;
    private final JPanel backContainer;

    private final Map<Class<? extends Flashcard>, FlashcardRenderer<?>> registry = new LinkedHashMap<>();

    public CardViewPanel(List<FlashcardRenderer<?>> renderers) {
        for (FlashcardRenderer<?> renderer : renderers) {
            registry.put(renderer.getSupportedType(), renderer);
        }

        this.setLayout(new GridBagLayout());
        this.putClientProperty(FlatClientProperties.STYLE_CLASS, "CardViewPanel");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        gbc.weighty = 0.0;
        frontContainer = new JPanel(new BorderLayout());
        frontContainer.setOpaque(false);
        this.add(frontContainer, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(15, 0, 15, 0);
        separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setVisible(false);
        this.add(separator, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        backContainer = new JPanel(new BorderLayout());
        backContainer.setOpaque(false);
        backContainer.setVisible(false);
        this.add(backContainer, gbc);

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(Box.createVerticalGlue(), gbc);
    }

    @SuppressWarnings("unchecked")
    private <T extends Flashcard> FlashcardRenderer<T> getRenderer(T card) {
        return (FlashcardRenderer<T>) registry.get(card.getClass());
    }

    public void setCard(Flashcard card) {
        frontContainer.removeAll();
        backContainer.removeAll();
        separator.setVisible(false);
        backContainer.setVisible(false);

        if (card != null) {
            var renderer = getRenderer(card);

            frontContainer.add(renderer.createFrontView(card), BorderLayout.CENTER);
            backContainer.add(renderer.createBackView(card), BorderLayout.CENTER);
        }

        this.revalidate();
        this.repaint();

    }

    public void showBack() {
        separator.setVisible(true);
        backContainer.setVisible(true);
        this.revalidate();
        this.repaint();
    }
}
