package com.wolfycz1.astradeck.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.wolfycz1.astradeck.algorithm.ReviewGrade;
import com.wolfycz1.astradeck.event.NewCardPresentedEvent;
import com.wolfycz1.astradeck.logic.StudySessionManager;
import com.wolfycz1.astradeck.ui.renderers.FlashcardRenderer;
import com.wolfycz1.astradeck.ui.renderers.ImageCardRenderer;
import com.wolfycz1.astradeck.ui.renderers.TextCardRenderer;
import com.wolfycz1.astradeck.ui.util.ImageProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Main panel for study sessions. Displays flashcards, study progress and processes grading.
 * @author wolfycz1
 */
@SuppressWarnings("ExtractMethodRecommender")
public class StudyPanel extends JPanel {
    private final StudySessionManager studySessionManager;
    private final EventBus eventBus;

    private final JProgressBar progressBar;
    private final JLabel remainingLabel;
    private final CardViewPanel cardViewPanel;

    private final JPanel footerPanel;
    private final CardLayout footerCardLayout;

    private static final String STATE_QUESTION = "QUESTION";
    private static final String STATE_ANSWER = "ANSWER";
    private String currentState = STATE_QUESTION;

    /**
     * Constructs a new study panel for the study session
     */
    public StudyPanel(StudySessionManager studySessionManager, EventBus eventBus, ImageProvider imageProvider) {
        this.studySessionManager = studySessionManager;
        this.eventBus = eventBus;
        this.eventBus.register(this);

        this.setLayout(new BorderLayout(20, 20));
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton abortButton = new JButton("Abort Session");
        abortButton.setFocusable(false);
        abortButton.putClientProperty(FlatClientProperties.STYLE, "hoverBorderColor: #FF4444");
        abortButton.addActionListener(_ -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to abort this study session?\nYour progress so far will be saved.",
                    "Abort Session",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                studySessionManager.abortSession();
            }
        });

        progressBar = new JProgressBar();
        remainingLabel = new JLabel("Remaining: 0");

        JLabel deckTitle = new JLabel(studySessionManager.getDeckTitle());
        deckTitle.setHorizontalAlignment(JLabel.CENTER);
        deckTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +12");

        JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
        headerPanel.add(progressBar, BorderLayout.NORTH);
        headerPanel.add(abortButton, BorderLayout.WEST);
        headerPanel.add(deckTitle, BorderLayout.CENTER);
        headerPanel.add(remainingLabel, BorderLayout.EAST);
        this.add(headerPanel, BorderLayout.NORTH);

        List<FlashcardRenderer<?>> renderers = List.of(new TextCardRenderer(), new ImageCardRenderer(imageProvider));
        cardViewPanel = new CardViewPanel(renderers);
        cardViewPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                revealAnswer();
            }
        });
        this.add(cardViewPanel, BorderLayout.CENTER);

        footerCardLayout = new CardLayout();
        footerPanel = new JPanel(footerCardLayout);

        JPanel questionFooter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton showAnswerButton = new JButton("Show answer (SPACE)");
        showAnswerButton.addActionListener(_ -> revealAnswer());
        showAnswerButton.setFocusable(false);
        questionFooter.add(showAnswerButton);

        JPanel answerFooter = new JPanel(new GridLayout(1, 4, 15, 0));
        answerFooter.add(createGradeButton("Again (1)", ReviewGrade.BLACKOUT,
                "outlineColor: #FF4444; borderWidth: 0; hoverBackground: darken(#FF4444, 15%)"));
        answerFooter.add(createGradeButton("Hard (2)", ReviewGrade.HARD,
                "outlineColor: #FFBB33; borderWidth: 0; hoverBackground: darken(#FFBB33, 15%)"));
        answerFooter.add(createGradeButton("Good (3)", ReviewGrade.GOOD,
                "outlineColor: #00C851; borderWidth: 0; hoverBackground: darken(#00C851, 15%)"));
        answerFooter.add(createGradeButton("Easy (4)", ReviewGrade.EASY,
                "outlineColor: #33B5E5; borderWidth: 0; hoverBackground: darken(#33B5E5, 15%)"));

        footerPanel.add(questionFooter, STATE_QUESTION);
        footerPanel.add(answerFooter, STATE_ANSWER);
        this.add(footerPanel, BorderLayout.SOUTH);

        setupKeybinds();
    }

    /**
     * Constructs a new grade button
     * @param text text on the button
     * @param reviewGrade review grade assigned to the button
     * @param style style of the button
     * @return the constructed grade button
     */
    private JButton createGradeButton(String text, ReviewGrade reviewGrade, String style) {
        JButton button = new JButton(text);
        button.addActionListener(_ -> processGrade(reviewGrade));
        button.putClientProperty(FlatClientProperties.STYLE, style);
        return button;
    }

    /**
     * Reveals the answer side of the flashcard
     */
    private void revealAnswer() {
        cardViewPanel.showBack();
        footerCardLayout.show(footerPanel, STATE_ANSWER);
        currentState = STATE_ANSWER;
    }

    /**
     * Submits a grade to be processed
     * @param reviewGrade grade to be processed
     */
    private void processGrade(ReviewGrade reviewGrade) {
        studySessionManager.processAnswer(reviewGrade);
    }

    /**
     * Handles presenting a new card during a study session
     * @param event event containing the card to display
     */
    @Subscribe
    public void onNewCard(NewCardPresentedEvent event) {
        cardViewPanel.setCard(event.card());
        footerCardLayout.show(footerPanel, STATE_QUESTION);

        currentState = STATE_QUESTION;

        int total = studySessionManager.getTotalCardsDue();
        int remaining = event.remainingCards();
        int completed = total - remaining;

        progressBar.setMaximum(total);
        progressBar.setValue(completed);
        remainingLabel.setText("Remaining: " + remaining);
    }

    /**
     * Sets up keybinds
     * 1, 2, 3, 4 - grading
     * SPACE, ENTER - show answer
     */
    private void setupKeybinds() {
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "showAnswer");
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "showAnswer");
        actionMap.put("showAnswer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentState.equals(STATE_QUESTION)) revealAnswer();
            }
        });

        bindGradeKey(inputMap, actionMap, "1", ReviewGrade.BLACKOUT);
        bindGradeKey(inputMap, actionMap, "2", ReviewGrade.HARD);
        bindGradeKey(inputMap, actionMap, "3", ReviewGrade.GOOD);
        bindGradeKey(inputMap, actionMap, "4", ReviewGrade.EASY);
    }

    /**
     * Binds a keybind to a review grade action
     * @param inputMap input map of the component
     * @param actionMap action map of the component
     * @param key key to bind
     * @param reviewGrade review grade to bind the key to
     */
    private void bindGradeKey(InputMap inputMap, ActionMap actionMap, String key, ReviewGrade reviewGrade) {
        inputMap.put(KeyStroke.getKeyStroke(key), "grade" + key);
        actionMap.put("grade" + key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentState.equals(STATE_ANSWER)) processGrade(reviewGrade);
            }
        });
    }

    /**
     * Ensures the panels unregisters itself from the eventbus
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        try {
            eventBus.unregister(this);
        } catch (IllegalArgumentException _) {}
    }
}
