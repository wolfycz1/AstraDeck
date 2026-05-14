package com.wolfycz1.astradeck.ui.editors;

import com.wolfycz1.astradeck.model.TextCard;
import com.wolfycz1.astradeck.model.sides.TextSide;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class TextCardEditor implements FlashcardEditor<TextCard> {
    private final JPanel panel;
    private final JTextArea frontText, backText;
    private Runnable changeListener;

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

    private void notifyChange() {
        if (changeListener != null) changeListener.run();
    }

    @Override
    public Class<TextCard> getSupportedType() {
        return TextCard.class;
    }

    @Override
    public String getDisplayName() {
        return "Text Card";
    }

    @Override
    public TextCard createNewCard() {
        return new TextCard();
    }

    @Override
    public JPanel getUI() {
        return panel;
    }

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

    @Override
    public void saveTo(TextCard card) {
        if (card.getFront() == null) card.setFront(new TextSide());
        card.getFront().setText(frontText.getText());

        if (card.getBack() == null) card.setBack(new TextSide());
        card.getBack().setText(backText.getText());
    }

    @Override
    public void setChangeListener(Runnable onChange) {
        this.changeListener = onChange;
    }

    @Override
    public void requestFocus() {
        if (frontText != null) {
            frontText.requestFocusInWindow();
        }
    }
}
