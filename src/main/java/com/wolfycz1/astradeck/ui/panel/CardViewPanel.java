package com.wolfycz1.astradeck.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.wolfycz1.astradeck.model.Flashcard;
import com.wolfycz1.astradeck.ui.renderers.FlashcardRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

/**
 * Panel responsible for displaying the front and back views of a flashcard.
 * @author wolfycz1
 */
public class CardViewPanel extends JPanel {
    private final JPanel frontContainer;
    private final JSeparator separator;
    private final JPanel backContainer;

    private final Map<Class<? extends Flashcard>, FlashcardRenderer<?>> registry = new LinkedHashMap<>();

    /**
     * Constructs the panel, initializes the UI layout, and populates the renderer registry.
     * @param renderers List of available renderers
     */
    public CardViewPanel(List<FlashcardRenderer<?>> renderers) {
        for (FlashcardRenderer<?> renderer : renderers) {
            registry.put(renderer.getSupportedType(), renderer);
        }

        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.putClientProperty(FlatClientProperties.STYLE, "arc: 16;" +
                "background: $EditorPane.background;");

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

    /**
     * Retrieves the appropriate renderer for the given flashcard type
     * @param card The flashcard to render
     * @return The corresponding renderer
     * @param <T> Type of the flashcard
     */
    @SuppressWarnings("unchecked")
    private <T extends Flashcard> FlashcardRenderer<T> getRenderer(T card) {
        return (FlashcardRenderer<T>) registry.get(card.getClass());
    }

    /**
     * Sets the current flashcard to be displayed
     * Renders front view, hides back view initially
     * @param card card to be displayed
     */
    public void setCard(Flashcard card) {
        frontContainer.removeAll();
        backContainer.removeAll();
        separator.setVisible(false);
        backContainer.setVisible(false);

        if (card != null) {
            var renderer = getRenderer(card);

            if (renderer == null) {
                frontContainer.add(new JLabel("Unsupported card type: " + card.getClass().getSimpleName()), BorderLayout.CENTER);
                return;
            }

            frontContainer.add(renderer.createFrontView(card), BorderLayout.CENTER);
            backContainer.add(renderer.createBackView(card), BorderLayout.CENTER);
        }

        this.revalidate();
        this.repaint();

    }

    /**
     * Reveals the back of the card
     */
    public void showBack() {
        separator.setVisible(true);
        backContainer.setVisible(true);
        this.revalidate();
        this.repaint();
    }
}
