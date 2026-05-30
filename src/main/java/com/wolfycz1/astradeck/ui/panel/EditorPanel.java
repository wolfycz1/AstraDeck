package com.wolfycz1.astradeck.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.common.eventbus.EventBus;
import com.wolfycz1.astradeck.event.*;
import com.wolfycz1.astradeck.model.Deck;
import com.wolfycz1.astradeck.model.Flashcard;
import com.wolfycz1.astradeck.model.ReviewState;
import com.wolfycz1.astradeck.ui.editors.FlashcardEditor;
import com.wolfycz1.astradeck.ui.renderers.FlashcardListRenderer;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Panel responsible for editing deck's flashcards
 * @author wolfycz1
 */
@SuppressWarnings("ExtractMethodRecommender")
public class EditorPanel extends JPanel {
    private final Deck deck;
    private final EventBus eventBus;

    private final DefaultListModel<Flashcard> listModel;
    private final JList<Flashcard> cardList;
    private final CardLayout cardLayout;
    private final JPanel rightFormPanel;

    public static final String VIEW_EMPTY = "EMPTY";
    private boolean isLoading = false;
    private final Map<Class<? extends Flashcard>, FlashcardEditor<?>> registry = new LinkedHashMap<>();

    private final Timer debounceTimer;
    private Flashcard pendingCard = null;

    /**
     * Constructs the editor panel and sets up the editors
     * @param deck deck to create an editor for
     * @param registeredEditors list of registered editors
     */
    public EditorPanel(Deck deck, EventBus eventBus, List<FlashcardEditor<?>> registeredEditors) {
        this.deck = deck;
        this.eventBus = eventBus;
        this.listModel = new DefaultListModel<>();
        this.cardList = new JList<>(listModel);
        this.cardLayout = new CardLayout();
        this.rightFormPanel = new JPanel(cardLayout);

        this.rightFormPanel.add(createEmptyStateView(), VIEW_EMPTY);
        for (FlashcardEditor<?> editor : registeredEditors) {
            registry.put(editor.getSupportedType(), editor);

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.add(editor.getUI(), BorderLayout.CENTER);
            wrapper.add(createDeleteFooter(), BorderLayout.SOUTH);

            this.rightFormPanel.add(wrapper, editor.getSupportedType().getName());

            editor.setChangeListener(this::autoSaveCurrentCard);
        }
        cardLayout.show(rightFormPanel, VIEW_EMPTY);

        this.debounceTimer = new Timer(750, _ -> flushPendingSave());
        this.debounceTimer.setRepeats(false);

        this.setLayout(new BorderLayout());
        this.add(createToolbar(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createSidebar(), rightFormPanel);
        splitPane.setDividerLocation(250);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        splitPane.putClientProperty(FlatClientProperties.SPLIT_PANE_EXPANDABLE_SIDE, null);

        this.add(splitPane, BorderLayout.CENTER);

        loadCardsIntoList();
        setupListeners();
    }

    /**
     * Constructs the toolbar with navigation controls
     * @return the constructed toolbar
     */
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JButton backButton = new JButton("← Dashboard");
        backButton.setFocusable(false);
        backButton.addActionListener(_ -> {
            flushPendingSave();
            eventBus.post(new DeckUpdatedEvent(deck));
            eventBus.post(new ReturnToDashboardEvent());
        });
        toolbar.add(backButton, BorderLayout.WEST);

        JLabel title = new JLabel("Editing " + deck.getTitle(), SwingConstants.CENTER);
        title.putClientProperty(FlatClientProperties.STYLE, "font: bold +3");
        toolbar.add(title, BorderLayout.CENTER);

        JButton settingsButton = new JButton("⚙ Settings");
        settingsButton.setFocusable(false);
        settingsButton.addActionListener(_ -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(parentWindow, "Deck Settings", Dialog.ModalityType.APPLICATION_MODAL);
            DeckSettingsPanel deckSettingsPanel = new DeckSettingsPanel(deck, eventBus, dialog);

            dialog.setContentPane(deckSettingsPanel);
            dialog.setSize(450, 350);
            dialog.setLocationRelativeTo(this);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);

            title.setText("Editing " + deck.getTitle());
        });
        toolbar.add(settingsButton, BorderLayout.EAST);

        return toolbar;
    }

    /**
     * Constructs the sidebar with the flashcard list
     * @return the constructed sidebar
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 10));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        cardList.setCellRenderer(new FlashcardListRenderer());

        JScrollPane scrollPane = new JScrollPane(cardList);
        scrollPane.setBorder(null);
        sidebar.add(scrollPane, BorderLayout.CENTER);

        JButton addButton = new JButton("+ Add new card");
        addButton.setFocusable(false);
        addButton.addActionListener(_ -> addNewCard());

        sidebar.add(addButton, BorderLayout.SOUTH);

        return sidebar;
    }

    /**
     * Clears the card list and repopulates it with flashcards
     */
    private void loadCardsIntoList() {
        listModel.clear();
        listModel.addAll(deck.getCardMap().values());
    }

    /**
     * Creates a placeholder view when a flashcard isn't selected
     * @return the constructed placeholder view
     */
    private JPanel createEmptyStateView() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel("Select a card from the list or add a new one.");
        label.putClientProperty(FlatClientProperties.STYLE_CLASS, "h3");
        label.setForeground(UIManager.getColor("Label.disabledForeground"));
        panel.add(label);
        return panel;
    }

    /**
     * Creates a footer with the delete action button
     * @return the constructed footer
     */
    private JPanel createDeleteFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton deleteButton = new JButton("Delete Card");
        deleteButton.setFocusable(false);
        deleteButton.putClientProperty(FlatClientProperties.STYLE, "background: #d32f2f; foreground: #ffffff;" +
                "hoverBackground: #b71c1c; focusedBackground: #c62828");
        deleteButton.addActionListener(_ -> deleteSelectedCard());
        footer.add(deleteButton);

        return footer;
    }

    /**
     * Sets up listeners to switch editors on switching flashcards
     */
    private void setupListeners() {
        cardList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            flushPendingSave();

            isLoading = true;
            try {
                Flashcard selectedCard = cardList.getSelectedValue();

                if (selectedCard == null) {
                    cardLayout.show(rightFormPanel, VIEW_EMPTY);
                } else {
                    var editor = getEditorForCard(selectedCard);
                    editor.populate(selectedCard);
                    cardLayout.show(rightFormPanel, selectedCard.getClass().getName());
                }
            } finally {
                isLoading = false;
            }
        });
    }

    /**
     * Retrieves the appropriate editor for a flashcard
     * @param card the flashcard to get the editor for
     * @return the editor for the flashcard
     * @param <T> type of the flashcard
     */
    @SuppressWarnings("unchecked")
    private <T extends Flashcard> FlashcardEditor<T> getEditorForCard(T card) {
        return (FlashcardEditor<T>) registry.get(card.getClass());
    }

    /**
     * Extracts data from the form, autosaving it into the model
     */
    private void autoSaveCurrentCard() {
        if (isLoading) return;
        Flashcard selectedCard = cardList.getSelectedValue();
        if (selectedCard != null) {
            var editor = getEditorForCard(selectedCard);
            editor.saveTo(selectedCard);
            deck.updateCardContent(selectedCard);
            cardList.repaint();

            pendingCard = selectedCard;
            debounceTimer.restart();
        }
    }

    /**
     * Prompts for a card type and creates a new flashcard of that type
     */
    private void addNewCard() {
        flushPendingSave();
        FlashcardEditor<?>[] editors = registry.values().toArray(new FlashcardEditor[0]);
        if (editors.length == 0) {
            JOptionPane.showMessageDialog(this, "No flashcard types are currently installed.");
            return;
        }

        Object[] options = Arrays.stream(editors).map(FlashcardEditor::getDisplayName).toArray();

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        int choice = JOptionPane.showOptionDialog(parentWindow, "Select the type of flashcard to create:",
                "New Flashcard", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                options, null);

        if (choice == JOptionPane.DEFAULT_OPTION) return;

        FlashcardEditor<?> selectedEditor = editors[choice];
        Flashcard newCard = selectedEditor.createNewCard();

        ReviewState initialReviewState = new ReviewState();
        initialReviewState.setCardId(newCard.getId());

        deck.addCard(newCard, initialReviewState);

        eventBus.post(new FlashcardUpdatedEvent(deck.getId(), newCard));
        eventBus.post(new ReviewStateUpdatedEvent(initialReviewState));

        listModel.addElement(newCard);
        cardList.setSelectedValue(newCard, true);

        selectedEditor.requestFocus();
    }

    /**
     * Prompts for confirmation, then deletes the selected flashcard from the deck
     */
    private void deleteSelectedCard() {
        Flashcard selectedCard = cardList.getSelectedValue();
        if (selectedCard == null) return;

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        int confirm = JOptionPane.showConfirmDialog(parentWindow, "Delete this flashcard permanently?", "Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (selectedCard.equals(pendingCard)) {
                debounceTimer.stop();
                pendingCard = null;
            }

            deck.removeCard(selectedCard.getId());
            listModel.removeElement(selectedCard);

            eventBus.post(new FlashcardDeletedEvent(deck.getId(), selectedCard.getId()));
        }
    }

    /**
     * Forces pending autosave to execute immediately
     */
    private void flushPendingSave() {
        if (pendingCard != null) {
            debounceTimer.stop();
            eventBus.post(new FlashcardUpdatedEvent(deck.getId(), pendingCard));
            pendingCard = null;
        }
    }
}
