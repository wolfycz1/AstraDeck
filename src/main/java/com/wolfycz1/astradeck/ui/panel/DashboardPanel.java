package com.wolfycz1.astradeck.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.wolfycz1.astradeck.event.RequestExportEvent;
import com.wolfycz1.astradeck.event.SessionAbortedEvent;
import com.wolfycz1.astradeck.event.SessionFinishedEvent;
import com.wolfycz1.astradeck.model.Deck;
import com.wolfycz1.astradeck.storage.AstraArchiveHandler;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ExtractMethodRecommender")
public class DashboardPanel extends JPanel {
    private final EventBus eventBus;
    private final AstraArchiveHandler astraArchiveHandler;

    private final List<Deck> loadedDecks = new ArrayList<>();

    private final JPanel gridContainer;

    public DashboardPanel(EventBus eventBus, AstraArchiveHandler astraArchiveHandler) {
        this.eventBus = eventBus;
        this.astraArchiveHandler = astraArchiveHandler;

        this.eventBus.register(this);

        this.setLayout(new BorderLayout());
        this.putClientProperty(FlatClientProperties.STYLE_CLASS, "DashboardPanel");

        JPanel navigationBar = new JPanel(new BorderLayout());
        navigationBar.putClientProperty(FlatClientProperties.STYLE_CLASS, "navigationBar");

        JLabel logoLabel = new JLabel("AstraDeck");
        logoLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "logoLabel");
        navigationBar.add(logoLabel, BorderLayout.WEST);

        JPanel navigationActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));

        JButton importButton = new JButton("Import deck");
        importButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "standard");
        importButton.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("AstraDeck decks (*.astra)", "astra"));
            fileChooser.setDialogTitle("Import Deck");
            Window parentWindow = SwingUtilities.getWindowAncestor(this);

            if (fileChooser.showOpenDialog(parentWindow) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    Deck importedDeck = astraArchiveHandler.importAstraArchive(selectedFile.toPath());
                    loadedDecks.add(importedDeck);
                    refreshGrid();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentWindow, "Failed to import deck:\n" + ex.getMessage(),
                            "Import Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        navigationActions.add(importButton);

        JButton newButton = new JButton("New deck");
        newButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "standard");
        newButton.addActionListener(_ -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            JOptionPane.showMessageDialog(parentWindow, "Not yet implemented.", "AstraDeck", JOptionPane.INFORMATION_MESSAGE);
        });
        navigationActions.add(newButton);

        navigationBar.add(navigationActions, BorderLayout.EAST);

        this.add(navigationBar, BorderLayout.NORTH);

        gridContainer = new JPanel(new GridLayout(0, 2, 20, 20));
        gridContainer.putClientProperty(FlatClientProperties.STYLE_CLASS, "gridContainer");

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.putClientProperty(FlatClientProperties.STYLE_CLASS, "gridWrapper");
        gridWrapper.add(gridContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        scrollPane.putClientProperty(FlatClientProperties.STYLE_CLASS, "scrollPane");
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(scrollPane, BorderLayout.CENTER);

        refreshGrid();
    }

    @Subscribe
    public void onSessionFinished(SessionFinishedEvent event) {
        refreshGrid();
    }

    @Subscribe
    public void onSessionAborted(SessionAbortedEvent event) {
        refreshGrid();
    }

    @Subscribe
    public void onExportRequest(RequestExportEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export deck");

        fileChooser.setSelectedFile(new File(event.deck().getTitle() + ".astra"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("AstraDeck decks (*.astra)", "astra"));
        Window parentWindow = SwingUtilities.getWindowAncestor(this);

        if (fileChooser.showSaveDialog(parentWindow) == JFileChooser.APPROVE_OPTION) {
            File targetFile = fileChooser.getSelectedFile();

            if (!targetFile.getName().endsWith(".astra")) {
                targetFile = new File(targetFile.getAbsolutePath() + ".astra");
            }

            try {
                astraArchiveHandler.exportAstraArchive(event.deck(), targetFile.toPath());
                JOptionPane.showMessageDialog(parentWindow, "Deck exported to:\n" + targetFile.getAbsolutePath(),
                        "Export complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parentWindow, "Failed to import deck:\n" + ex.getMessage(),
                        "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshGrid() {
        gridContainer.removeAll();

        if (loadedDecks.isEmpty()) {
            JLabel emptyLabel = new JLabel("No decks loaded.");
            emptyLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "emptyLabel");
            gridContainer.add(emptyLabel);
        } else {
            for (Deck deck : loadedDecks) {
                gridContainer.add(new DeckWidgetPanel(deck, eventBus, () -> {
                    loadedDecks.remove(deck);
                    refreshGrid();
                }));
            }
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }
}
