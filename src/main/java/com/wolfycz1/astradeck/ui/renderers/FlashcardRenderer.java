package com.wolfycz1.astradeck.ui.renderers;

import com.wolfycz1.astradeck.model.Flashcard;

import javax.swing.*;

/**
 * Defines the contract for visualising a flashcard
 * @param <T> a flashcard object that extends {@link Flashcard}
 * @author wolfycz1
 */
public interface FlashcardRenderer<T extends Flashcard> {
    /** Returns the class type this renderer supports **/
    Class<T> getSupportedType();

    /** Builds the UI panel for the front side of the flashcard **/
    JPanel createFrontView(T card);

    /** Builds the UI panel for the back side of the flashcard **/
    JPanel createBackView(T card);

    default String formatCardText(String text) {
        return "<html><center>" + text.replace("\n", "<br>") + "</center></html>";
    }
}
