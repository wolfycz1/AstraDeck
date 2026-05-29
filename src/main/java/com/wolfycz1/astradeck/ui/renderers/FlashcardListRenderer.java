package com.wolfycz1.astradeck.ui.renderers;

import com.wolfycz1.astradeck.model.Flashcard;

import javax.swing.*;
import java.awt.*;

/**
 * Customizes how flashcards are displayed as text items inside a {@link JList}
 * @author wolfycz1
 */
public class FlashcardListRenderer extends DefaultListCellRenderer {
    /**
     * Extracts a preview snippet from the flashcard and formats it for the list row
     * @param list The JList we're painting.
     * @param value The value returned by list.getModel().getElementAt(index).
     * @param index The cells index.
     * @param isSelected True if the specified cell was selected.
     * @param cellHasFocus True if the specified cell has the focus.
     * @return a {@link Component}
     */
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof Flashcard card) {
            String text = card.getPreviewText();
            setText(truncate(text));
        }

        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return this;
    }

    /**
     * Strips line breaks and cuts off long text so it fits the list
     * @param text text to truncate
     * @return truncated text
     */
    private String truncate(String text) {
        String truncated = text.trim().replace("\n", " ");
        if (truncated.length() > 35) {
            return truncated.substring(0, 32) + "...";
        }
        return truncated;
    }
}
