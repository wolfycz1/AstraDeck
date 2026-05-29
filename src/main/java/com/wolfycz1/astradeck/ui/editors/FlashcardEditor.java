package com.wolfycz1.astradeck.ui.editors;

import com.wolfycz1.astradeck.model.Flashcard;

import javax.swing.*;

/**
 * Defines the contract for all flashcard editors
 * @param <T> a flashcard object that extends {@link Flashcard}
 * @author wolfycz1
 */
public interface FlashcardEditor<T extends Flashcard> {
    /** Returns the class type this editor supports **/
    Class<T> getSupportedType();

    /** Returns the display name of the editor **/
    String getDisplayName();

    /** Instantiates a new card of the supported type **/
    T createNewCard();

    /** Returns the main {@link JPanel} containing the editor **/
    JPanel getUI();

    /** Populates the editor fields with the card data **/
    void populate(T card);

    /** Saves the editor fields to the card **/
    void saveTo(T card);

    /** Registers a callback on whenever a change is made **/
    void setChangeListener(Runnable onChange);

    /** Puts the cursor in the primary field of the editor **/
    void requestFocus();
}
