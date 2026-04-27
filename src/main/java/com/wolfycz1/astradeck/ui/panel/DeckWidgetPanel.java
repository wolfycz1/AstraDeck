package com.wolfycz1.astradeck.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.common.eventbus.EventBus;
import com.wolfycz1.astradeck.event.RequestEditEvent;
import com.wolfycz1.astradeck.event.RequestExportEvent;
import com.wolfycz1.astradeck.event.RequestStudyEvent;
import com.wolfycz1.astradeck.model.Deck;
import com.wolfycz1.astradeck.model.ReviewState;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;

@SuppressWarnings("ExtractMethodRecommender")
public class DeckWidgetPanel extends JPanel {
    public DeckWidgetPanel(Deck deck, EventBus eventBus, Runnable onDelete) {
        this.setLayout(new BorderLayout(15, 15));
        this.putClientProperty(FlatClientProperties.STYLE_CLASS, "DeckWidget");

        long dueCards = deck.getReviewData().values().stream()
                .map(ReviewState::getNextReviewDate)
                .filter(date -> date != null && date.isBefore(Instant.now()))
                .count();

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.putClientProperty(FlatClientProperties.STYLE_CLASS, "infoPanel");

        JLabel titleLabel = new JLabel(deck.getTitle());
        titleLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "titleLabel");
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(10));

        JLabel totalLabel = new JLabel("Total cards: " + deck.getCardMap().size());
        totalLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "subtitleLabel");
        infoPanel.add(totalLabel);
        infoPanel.add(Box.createVerticalStrut(5));

        JLabel dueLabel = new JLabel(dueCards > 0 ? "Due today: " + dueCards : "All caught up.");
        dueLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, dueCards > 0 ? "dueLabel" : "doneLabel");
        infoPanel.add(dueLabel);

        this.add(infoPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(1, 4, 5, 0));
        actionPanel.putClientProperty(FlatClientProperties.STYLE_CLASS, "actionPanel");

        JButton studyButton = new JButton("Study");
        studyButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "standard");
        studyButton.setEnabled(dueCards > 0);
        studyButton.addActionListener(_ -> eventBus.post(new RequestStudyEvent(deck)));
        actionPanel.add(studyButton);

        JButton editButton = new JButton("Edit");
        editButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "standard");
        editButton.addActionListener(_ -> eventBus.post(new RequestEditEvent(deck)));
        actionPanel.add(editButton);

        JButton exportButton = new JButton("Export");
        exportButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "standard");
        exportButton.addActionListener(_ -> eventBus.post(new RequestExportEvent(deck)));
        actionPanel.add(exportButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "deleteButton");
        deleteButton.addActionListener(_ -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            int confirm = JOptionPane.showConfirmDialog(parentWindow,
                    "Delete '" + deck.getTitle() + "' from memory?",
                    "Delete deck", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                onDelete.run();
            }
        });
        actionPanel.add(deleteButton);

        this.add(actionPanel, BorderLayout.SOUTH);
    }
}
