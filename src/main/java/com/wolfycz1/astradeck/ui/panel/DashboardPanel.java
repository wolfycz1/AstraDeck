package com.wolfycz1.astradeck.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.wolfycz1.astradeck.event.*;
import com.wolfycz1.astradeck.model.Deck;
import com.wolfycz1.astradeck.network.RemoteRepositoryService;
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

/**
 * Dashboard panel displaying a grid of avilable decks and navigation through the app
 * @author wolfycz1
 */
@SuppressWarnings("ExtractMethodRecommender")
@Slf4j
public class DashboardPanel extends JPanel {
    private final EventBus eventBus;
    private final AstraArchiveHandler astraArchiveHandler;

    private final List<Deck> loadedDecks = new ArrayList<>();

    private final JPanel gridContainer;

    /**
     * Constructs the DashboardPanel, setting up the navigation bar and the deck grid
     */
    public DashboardPanel(EventBus eventBus, AstraArchiveHandler astraArchiveHandler, List<Deck> initialDecks) {
        this.eventBus = eventBus;
        this.astraArchiveHandler = astraArchiveHandler;
        this.loadedDecks.addAll(initialDecks);

        this.eventBus.register(this);

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JPanel navigationBar = new JPanel(new BorderLayout());
        navigationBar.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel logoLabel = new JLabel("AstraDeck");
        logoLabel.putClientProperty(FlatClientProperties.STYLE, "font: +10 bold" +
                "foreground: $Component.accentColor");
        navigationBar.add(logoLabel, BorderLayout.WEST);

        JPanel navigationActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));

        JButton discoverButton = new JButton("Discover");
        discoverButton.setFocusable(false);
        discoverButton.addActionListener(_ -> handleDiscovery());
        navigationActions.add(discoverButton);

        JButton importButton = new JButton("Import deck");
        importButton.setFocusable(false);
        importButton.addActionListener(_ -> handleImport());
        navigationActions.add(importButton);

        JButton newButton = new JButton("New deck");
        newButton.setFocusable(false);
        newButton.addActionListener(_ -> newDeck());
        navigationActions.add(newButton);

        navigationBar.add(navigationActions, BorderLayout.EAST);

        this.add(navigationBar, BorderLayout.NORTH);

        gridContainer = new JPanel(new GridLayout(0, 2, 20, 20));
        gridContainer.setOpaque(false);

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        gridWrapper.add(gridContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(scrollPane, BorderLayout.CENTER);

        refreshGrid();
    }

    /**
     * Handles creating a new deck, asks for a name, then requests an editor
     */
    private void newDeck() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        String title = JOptionPane.showInputDialog(parentWindow, "Enter a title for the new deck:",
                "New Deck", JOptionPane.PLAIN_MESSAGE);
        if (title != null && !title.trim().isEmpty()) {
            Deck newDeck = new Deck();
            newDeck.setTitle(title.trim());
            loadedDecks.add(newDeck);
            refreshGrid();

            eventBus.post(new DeckUpdatedEvent(newDeck));
            eventBus.post(new RequestEditEvent(newDeck));
        }
    }

    /**
     * Handles importing an AstraDeck decks
     */
    private void handleImport() {
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
                eventBus.post(new DeckImportedEvent(importedDeck));
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
    }

    /**
     * Handles displaying the discovery dialog
     */
    private void handleDiscovery() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Avaiable Decks", Dialog.ModalityType.APPLICATION_MODAL);
        RemoteRepositoryService repositoryService = new RemoteRepositoryService();
        RepositoryPanel repositoryPanel = new RepositoryPanel(eventBus, astraArchiveHandler, repositoryService, loadedDecks);

        dialog.setContentPane(repositoryPanel);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    /**
     * Handles the event of a deck being imported, adding it to the list
     * @param event event containing the deck imported
     */
    @Subscribe
    public void onDeckImported(DeckImportedEvent event) {
        loadedDecks.add(event.deck());
        refreshGrid();
    }

    /**
     * Handles the event of a deck being modified from the editor
     */
    @Subscribe
    public void onDeckUpdated(DeckUpdatedEvent ignored) {
        refreshGrid();
    }

    /**
     * Handles the even of a deck being deleted, removing it from the list
     * @param event event containing the deck being removed
     */
    @Subscribe
    public void onDeckDeleted(DeckDeletedEvent event) {
        loadedDecks.removeIf(deck -> deck.getId().equals(event.deckId()));
        refreshGrid();
    }

    /**
     * Refreshed the dashboard on finishing a study session
     */
    @Subscribe
    public void onSessionFinished(SessionFinishedEvent ignored) {
        refreshGrid();
    }

    /**
     * Refreshes the dashboard on aborting a study session
     */
    @Subscribe
    public void onSessionAborted(SessionAbortedEvent ignored) {
        refreshGrid();
    }

    /**
     * Handles a request to export a deck
     * @param event event containing the deck being exported
     */
    @Subscribe
    public void onExportRequest(RequestExportEvent event) {
        handleExport(event.deck());
    }

    /**
     * Handles exporting an AstraArchive deck
     * @param deck the deck to be exported
     */
    private void handleExport(Deck deck) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export deck");

        String sanitizedTitle = sanitizeFilename(deck.getTitle());

        fileChooser.setSelectedFile(new File(sanitizedTitle + ".astra"));
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
                astraArchiveHandler.exportAstraArchive(deck, targetFile.toPath());
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

    /**
     * Removes invalid filename characters from a deck title
     * @param title the deck title to sanitize
     * @return sanitized filename
     */
    private String sanitizeFilename(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Untitled_Deck";
        }

        return title.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * Clears the current dashboard and repopulates it
     */
    private void refreshGrid() {
        gridContainer.removeAll();

        if (loadedDecks.isEmpty()) {
            JLabel emptyLabel = new JLabel("No decks loaded.");
            emptyLabel.putClientProperty(FlatClientProperties.STYLE, "font: +2 italic" +
                    "foreground: $Label.disabledForeground");
            gridContainer.add(emptyLabel);
        } else {
            for (Deck deck : loadedDecks) {
                gridContainer.add(new DeckWidgetPanel(deck, eventBus));
            }
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }
}
