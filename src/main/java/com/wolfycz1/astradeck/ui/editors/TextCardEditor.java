package com.wolfycz1.astradeck.ui.editors;

import com.wolfycz1.astradeck.model.TextCard;
import com.wolfycz1.astradeck.model.sides.TextSide;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * A basic editor panel for editing text-only flashcards
 * @author wolfycz1
 */
public class TextCardEditor implements FlashcardEditor<TextCard> {
    private final JPanel panel;
    private final JTextArea frontText, backText;
    private Runnable changeListener;

    /**
     * Sets up the editor view and its document listener
     */
    public TextCardEditor() {
        panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        frontText = new JTextArea();
        backText = new JTextArea();

        JScrollPane frontScroll = new JScrollPane(frontText);
        JScrollPane backScroll = new JScrollPane(backText);

        frontScroll.setBorder(BorderFactory.createTitledBorder("Front (Question)"));
        backScroll.setBorder(BorderFactory.createTitledBorder("Back (Answer)"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, frontScroll, backScroll);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        panel.add(splitPane, BorderLayout.CENTER);

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                notifyChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                notifyChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                notifyChange();
            }
        };

        frontText.getDocument().addDocumentListener(documentListener);
        backText.getDocument().addDocumentListener(documentListener);
    }

    /** Helper method to run the change listener whenever the text is modified. */
    private void notifyChange() {
        if (changeListener != null) changeListener.run();
    }

    /**
     * Returns the class type this editor supports
     * @return {@link TextCard} class
     */
    @Override
    public Class<TextCard> getSupportedType() {
        return TextCard.class;
    }

    /**
     * Returns the display name of the editor
     * @return "Text Card"
     */
    @Override
    public String getDisplayName() {
        return "Text Card";
    }

    /**
     * Instantiates a new card of the supported type
     * @return new {@link TextCard}
     */
    @Override
    public TextCard createNewCard() {
        return new TextCard();
    }

    /**
     * Returns the main {@link JPanel} containing the editor
     * @return main {@link JPanel}
     */
    @Override
    public JPanel getUI() {
        return panel;
    }

    /**
     * Populates the editor fields with the card data
     * @param card the card data
     */
    @Override
    public void populate(TextCard card) {
        if (card.getFront() != null) {
            frontText.setText(card.getFront().getText());
        } else {
            frontText.setText("");
        }
        if (card.getBack() != null) {
            backText.setText(card.getBack().getText());
        } else {
            backText.setText("");
        }
    }

    /**
     * Saves the editor fields to the card
     * @param card the {@link TextCard} object to save to
     */
    @Override
    public void saveTo(TextCard card) {
        if (card.getFront() == null) card.setFront(new TextSide());
        card.getFront().setText(frontText.getText());

        if (card.getBack() == null) card.setBack(new TextSide());
        card.getBack().setText(backText.getText());
    }

    /**
     * Registers a callback on whenever a change is made
     * @param onChange {@link Runnable} listener
     */
    @Override
    public void setChangeListener(Runnable onChange) {
        this.changeListener = onChange;
    }

    /**
     * Puts the cursor in the primary field of the editor
     */
    @Override
    public void requestFocus() {
        if (frontText != null) {
            frontText.requestFocusInWindow();
        }
    }
}
