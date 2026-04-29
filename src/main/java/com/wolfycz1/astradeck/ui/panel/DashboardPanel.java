package com.wolfycz1.astradeck.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.wolfycz1.astradeck.event.DeckUpdatedEvent;
import com.wolfycz1.astradeck.event.RequestExportEvent;
import com.wolfycz1.astradeck.event.SessionAbortedEvent;
import com.wolfycz1.astradeck.event.SessionFinishedEvent;
import com.wolfycz1.astradeck.model.Deck;
import com.wolfycz1.astradeck.storage.AstraArchiveHandler;
import com.wolfycz1.astradeck.storage.exceptions.InvalidDeckException;
import com.wolfycz1.astradeck.storage.exceptions.MissingMediaException;
import com.wolfycz1.astradeck.storage.exceptions.UnsupportedVersionException;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
                if (!selectedFile.getName().toLowerCase().endsWith(".astra")) {
                    log.warn("{} is not a valid .astra file.", selectedFile.getAbsolutePath());
                    JOptionPane.showMessageDialog(parentWindow,
                            "Please select a valid .astra file.",
                            "Invalid File Type", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    Deck importedDeck = astraArchiveHandler.importAstraArchive(selectedFile.toPath());
                    loadedDecks.add(importedDeck);
                    refreshGrid();
                    log.info("Imported deck: {}", importedDeck.getTitle());
                } catch (UnsupportedVersionException e) {
                    log.warn("Import failed; unsupported version: {}", e.getMessage());
                    JOptionPane.showMessageDialog(parentWindow,
                            "Version of the deck you're trying to import is unsupported.",
                            "Import Error", JOptionPane.ERROR_MESSAGE);
                } catch (InvalidDeckException | MissingMediaException e) {
                    log.warn("Import failed; deck corrupted: {}", e.getMessage());
                    JOptionPane.showMessageDialog(parentWindow,
                            "The deck you're trying to import is corrupted or invalid",
                            "Import Error", JOptionPane.ERROR_MESSAGE);
                } catch (IOException e) {
                    log.warn("Import failed; io exception: {} - {}", selectedFile.getAbsolutePath(), e.getMessage());
                    JOptionPane.showMessageDialog(parentWindow,
                            "The file you're trying to import could not be accessed or is invalid.",
                            "Import Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    log.error("Unexpected error while importing file: {} - {}", selectedFile.getAbsolutePath(), e.getMessage());
                    JOptionPane.showMessageDialog(parentWindow,
                            "An unexpected error occurred while importing the deck.",
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
    public void onDeckUpdated(DeckUpdatedEvent event) {
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

            if (!targetFile.getName().toLowerCase().endsWith(".astra")) {
                targetFile = new File(targetFile.getAbsolutePath() + ".astra");
            }

            if (targetFile.exists()) {
                int confirm = JOptionPane.showConfirmDialog(parentWindow,
                        "The file '" + targetFile.getName() + "' already exists.\nDo you want to overwrite it?",
                        "Overwrite?", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try {
                astraArchiveHandler.exportAstraArchive(event.deck(), targetFile.toPath());
                JOptionPane.showMessageDialog(parentWindow, "Deck exported to:\n" + targetFile.getAbsolutePath(),
                        "Export complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (MissingMediaException e) {
                log.warn("Export failed; missing media: {}", e.getMessage());
                JOptionPane.showMessageDialog(parentWindow,
                        "Could not export the deck because one or more media files are missing.",
                        "Export error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                log.warn("Export failed; io exception: {} - {}", targetFile.getAbsolutePath(), e.getMessage());
                JOptionPane.showMessageDialog(parentWindow,
                        "Failed to save the file. Please check your write permissions and ensure you have enough disk space.",
                        "Export error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                log.error("Unexpected error while exporting file: {} - {}", targetFile.getAbsolutePath(), e.getMessage());
                JOptionPane.showMessageDialog(parentWindow,
                        "An unexpected error occurred while exporting the deck.",
                        "Export Error", JOptionPane.ERROR_MESSAGE);
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
