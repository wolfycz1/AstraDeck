package com.wolfycz1.astradeck.ui.panel;

import com.wolfycz1.astradeck.model.Flashcard;

import javax.swing.*;
import java.awt.*;

public class FlashcardListRenderer extends DefaultListCellRenderer {
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

    private String truncate(String text) {
        String truncated = text.trim().replace("\n", " ");
        if (truncated.length() > 35) {
            return truncated.substring(0, 32) + "...";
        }
        return truncated;
    }
}
