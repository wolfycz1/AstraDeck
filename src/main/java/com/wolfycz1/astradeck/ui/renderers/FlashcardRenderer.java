package com.wolfycz1.astradeck.ui.renderers;

import com.wolfycz1.astradeck.model.Flashcard;

import javax.swing.*;

public interface FlashcardRenderer<T extends Flashcard> {
    Class<T> getSupportedType();

    JPanel createFrontView(T card);

    JPanel createBackView(T card);
}
