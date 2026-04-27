package com.wolfycz1.astradeck.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.wolfycz1.astradeck.algorithm.ReviewGrade;
import com.wolfycz1.astradeck.event.NewCardPresentedEvent;
import com.wolfycz1.astradeck.logic.MediaManager;
import com.wolfycz1.astradeck.logic.StudySessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

    public StudyPanel(StudySessionManager studySessionManager, EventBus eventBus, MediaManager mediaManager) {
        this.studySessionManager = studySessionManager;
        this.eventBus = eventBus;
        this.eventBus.register(this);

        this.setLayout(new BorderLayout(20, 20));
        this.putClientProperty(FlatClientProperties.STYLE_CLASS, "StudyPanel");

        JButton abortButton = new JButton("Abort Session");
        abortButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "abortButton");
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
        deckTitle.putClientProperty(FlatClientProperties.STYLE_CLASS, "deckTitle");

        JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
        headerPanel.add(progressBar, BorderLayout.NORTH);
        headerPanel.add(abortButton, BorderLayout.WEST);
        headerPanel.add(deckTitle, BorderLayout.CENTER);
        headerPanel.add(remainingLabel, BorderLayout.EAST);
        this.add(headerPanel, BorderLayout.NORTH);

        cardViewPanel = new CardViewPanel(mediaManager);
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
        showAnswerButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "standard");
        questionFooter.add(showAnswerButton);

        JPanel answerFooter = new JPanel(new GridLayout(1, 4, 15, 0));
        answerFooter.add(createGradeButton("Again (1)", ReviewGrade.BLACKOUT, "gradeAgain"));
        answerFooter.add(createGradeButton("Hard (2)", ReviewGrade.HARD, "gradeHard"));
        answerFooter.add(createGradeButton("Good (3)", ReviewGrade.GOOD, "gradeGood"));
        answerFooter.add(createGradeButton("Easy (4)", ReviewGrade.EASY, "gradeEasy"));

        footerPanel.add(questionFooter, STATE_QUESTION);
        footerPanel.add(answerFooter, STATE_ANSWER);
        this.add(footerPanel, BorderLayout.SOUTH);

        setupKeybinds();
    }

    private JButton createGradeButton(String text, ReviewGrade reviewGrade, String style) {
        JButton button = new JButton(text);
        button.addActionListener(_ -> processGrade(reviewGrade));
        button.putClientProperty(FlatClientProperties.STYLE_CLASS, style);
        return button;
    }

    private void revealAnswer() {
        cardViewPanel.showBack();
        footerCardLayout.show(footerPanel, STATE_ANSWER);
        currentState = STATE_ANSWER;
    }

    private void processGrade(ReviewGrade reviewGrade) {
        studySessionManager.processAnswer(reviewGrade);
    }

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

    private void bindGradeKey(InputMap inputMap, ActionMap actionMap, String key, ReviewGrade reviewGrade) {
        inputMap.put(KeyStroke.getKeyStroke(key), "grade" + key);
        actionMap.put("grade" + key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentState.equals(STATE_ANSWER)) processGrade(reviewGrade);
            }
        });
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        try {
            eventBus.unregister(this);
        } catch (IllegalArgumentException _) {}
    }
}
