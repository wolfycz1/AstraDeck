package com.wolfycz1.astradeck.ui.panel;

import com.google.common.eventbus.EventBus;
import com.wolfycz1.astradeck.event.DeckUpdatedEvent;
import com.wolfycz1.astradeck.model.Deck;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel providing a form for editting deck metadata
 * @author wolfycz1
 */
public class DeckSettingsPanel extends JPanel {
    private final Deck deck;
    private final EventBus eventBus;

    private JTextField titleField;
    private JTextField authorField;
    private JTextArea descriptionArea;
    private JTextField languagesField;

    /**
     * Constructs the settings form
     * @param deck deck to construct a settings form for
     * @param parentDialog dialog to contain the panel
     */
    public DeckSettingsPanel(Deck deck, EventBus eventBus, JDialog parentDialog) {
        this.deck = deck;
        this.eventBus = eventBus;

        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        this.add(createFormPanel(), BorderLayout.CENTER);
        this.add(createButtonPanel(parentDialog), BorderLayout.SOUTH);

        populateFields();
    }

    /**
     * Creates the main form layout
     * @return constructed panel
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        titleField = new JTextField();
        authorField = new JTextField();

        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);

        languagesField = new JTextField();
        languagesField.setToolTipText("Comma-separated list (e.g., English, Japanese)");

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(descriptionScrollPane, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(new JLabel("Languages:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(languagesField, gbc);

        return formPanel;
    }

    /**
     * Creates action buttons for saving and canceling
     * @param parentDialog dialog to contain the buttons
     * @return the panel containing the buttons
     */
    private JPanel createButtonPanel(JDialog parentDialog) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(_ -> parentDialog.dispose());
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton("Save Settings");
        saveButton.setFocusable(false);
        saveButton.addActionListener(_ -> saveSettings(parentDialog));
        buttonPanel.add(saveButton);

        return buttonPanel;
    }

    /**
     * Populates the fields with current metadata
     */
    private void populateFields() {
        titleField.setText(deck.getTitle() != null ? deck.getTitle() : "");
        authorField.setText(deck.getAuthor() != null ? deck.getAuthor() : "");
        descriptionArea.setText(deck.getDescription() != null ? deck.getDescription() : "");

        if (deck.getLanguages() != null && !deck.getLanguages().isEmpty()) {
            languagesField.setText(String.join(", ", deck.getLanguages()));
        }
    }

    /**
     * Saves the changes from the form
     * @param parentDialog dialog containing the form
     */
    private void saveSettings(JDialog parentDialog) {
        deck.setTitle(titleField.getText().trim());
        deck.setAuthor(authorField.getText().trim());
        deck.setDescription(descriptionArea.getText().trim());

        String langs = languagesField.getText().trim();
        if (!langs.isEmpty()) {
            List<String> langList = Arrays.stream(langs.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            deck.setLanguages(langList);
        } else {
            deck.getLanguages().clear();
        }

        deck.setUpdatedAt(Instant.now());

        eventBus.post(new DeckUpdatedEvent(deck));
        parentDialog.dispose();
    }
}
