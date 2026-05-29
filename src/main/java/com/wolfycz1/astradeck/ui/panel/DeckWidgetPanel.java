package com.wolfycz1.astradeck.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.common.eventbus.EventBus;
import com.wolfycz1.astradeck.event.*;
import com.wolfycz1.astradeck.model.Deck;
import com.wolfycz1.astradeck.model.ReviewState;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;

/**
 * Represents a single deck widget. Displays options and statistics.
 * @author wolfycz1
 */
@SuppressWarnings("ExtractMethodRecommender")
public class DeckWidgetPanel extends JPanel {

    /**
     * Constructs the deck widget
     * @param deck deck to construct a widget for
     */
    public DeckWidgetPanel(Deck deck, EventBus eventBus) {
        this.setLayout(new BorderLayout(15, 15));
        this.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: $TextArea.background;" +
                "border: 15,15,15,15,$Component.borderColor, 1,1,1,1");

        long dueCards = deck.getReviewData().values().stream()
                .map(ReviewState::getNextReviewDate)
                .filter(date -> date != null && date.isBefore(Instant.now()))
                .count();

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(deck.getTitle());
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +3");
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(10));

        JLabel totalLabel = new JLabel("Total cards: " + deck.getCardMap().size());
        totalLabel.putClientProperty(FlatClientProperties.STYLE, "font: -1;" +
                "foreground: $Label.disabledForeground");
        infoPanel.add(totalLabel);
        infoPanel.add(Box.createVerticalStrut(5));

        JLabel dueLabel = new JLabel(dueCards > 0 ? "Due today: " + dueCards : "All caught up.");
        if (dueCards > 0) {
            dueLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold; foreground: #e65100");
        } else {
            dueLabel.putClientProperty(FlatClientProperties.STYLE, "foreground: #2e7d32");
        }
        infoPanel.add(dueLabel);

        this.add(infoPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(1, 4, 5, 0));
        actionPanel.setOpaque(false);

        JButton studyButton = new JButton("Study");
        studyButton.setFocusable(false);
        studyButton.setEnabled(dueCards > 0);
        studyButton.addActionListener(_ -> eventBus.post(new RequestStudyEvent(deck)));
        actionPanel.add(studyButton);

        JButton editButton = new JButton("Edit");
        editButton.setFocusable(false);
        editButton.addActionListener(_ -> eventBus.post(new RequestEditEvent(deck)));
        actionPanel.add(editButton);

        JButton exportButton = new JButton("Export");
        exportButton.setFocusable(false);
        exportButton.addActionListener(_ -> eventBus.post(new RequestExportEvent(deck)));
        actionPanel.add(exportButton);

        JPopupMenu popupMenu = new JPopupMenu();

        JButton moreButton = new JButton("More ▾");
        moreButton.setFocusable(false);
        moreButton.addActionListener(_ -> popupMenu.show(moreButton, 0, moreButton.getHeight()));

        JMenuItem resetItem = new JMenuItem("Reset progress");
        resetItem.addActionListener(_ -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            int confirm = JOptionPane.showConfirmDialog(parentWindow,
                    "Reset all study progress for '" + deck.getTitle() + "'?\nThis will clear all intervals and cannot be undone.",
                    "Reset progress",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                deck.resetDeckProgress();
                eventBus.post(new DeckUpdatedEvent(deck));
                eventBus.post(new DeckResetEvent(deck.getId(), new ReviewState()));
            }
        });
        popupMenu.add(resetItem);
        popupMenu.addSeparator();

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.setFocusable(false);
        deleteItem.putClientProperty(FlatClientProperties.STYLE, "background: #d32f2f;" +
                "foreground: #ffffff; hoverBackground: #b71c1c; focusedBackground: #c62828");
        deleteItem.addActionListener(_ -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            int confirm = JOptionPane.showConfirmDialog(parentWindow,
                    "Delete '" + deck.getTitle() + "' from memory?",
                    "Delete deck", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                eventBus.post(new DeckDeletedEvent(deck.getId()));
            }
        });
        popupMenu.add(deleteItem);

        actionPanel.add(moreButton);

        this.add(actionPanel, BorderLayout.SOUTH);
    }
}
