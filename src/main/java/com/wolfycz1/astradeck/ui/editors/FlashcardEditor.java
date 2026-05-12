package com.wolfycz1.astradeck.ui.editors;

import com.wolfycz1.astradeck.model.Flashcard;

import javax.swing.*;

public interface FlashcardEditor<T extends Flashcard> {
    Class<T> getSupportedType();

    String getDisplayName();

    T createNewCard();

    JPanel getUI();

    void populate(T card);

    void saveTo(T card);

    void setChangeListener(Runnable onChange);

    void requestFocus();
}
