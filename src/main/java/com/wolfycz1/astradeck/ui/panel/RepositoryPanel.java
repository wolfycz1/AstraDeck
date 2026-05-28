package com.wolfycz1.astradeck.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.common.eventbus.EventBus;
import com.wolfycz1.astradeck.event.DeckImportedEvent;
import com.wolfycz1.astradeck.model.Deck;
import com.wolfycz1.astradeck.model.RepositoryDeck;
import com.wolfycz1.astradeck.network.RemoteRepositoryService;
import com.wolfycz1.astradeck.storage.AstraArchiveHandler;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RepositoryPanel extends JPanel {
    private final EventBus eventBus;
    private final AstraArchiveHandler astraArchiveHandler;
    private final RemoteRepositoryService remoteRepositoryService;

    private final JPanel gridContainer;
    private final JTextField searchField;
    private final JButton refreshButton;
    private List<RepositoryDeck> allRepositoryDecks = new ArrayList<>();
    private final List<Deck> existingDecks;

    private static final int[] KONAMI_CODE = {
        KeyEvent.VK_UP, KeyEvent.VK_UP,
        KeyEvent.VK_DOWN, KeyEvent.VK_DOWN,
        KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
        KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
        KeyEvent.VK_B, KeyEvent.VK_A,
        KeyEvent.VK_ENTER
    };

    public RepositoryPanel(EventBus eventBus, AstraArchiveHandler astraArchiveHandler, RemoteRepositoryService remoteRepositoryService, List<Deck> existingDecks) {
        this.eventBus = eventBus;
        this.astraArchiveHandler = astraArchiveHandler;
        this.remoteRepositoryService = remoteRepositoryService;
        this.existingDecks = existingDecks;

        this.setLayout(new BorderLayout());
        this.putClientProperty(FlatClientProperties.STYLE_CLASS, "RepositoryPanel");

        JPanel topBar = new JPanel(new BorderLayout(15, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("AstraDeck Decks");
        titleLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "logoLabel");
        topBar.add(titleLabel, BorderLayout.WEST);

        Timer debounceTimer = new Timer(300, _ -> filterDecks());
        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search by title, author, or tags...");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                debounceTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                debounceTimer.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                debounceTimer.restart();
            }
        });
        topBar.add(searchField, BorderLayout.CENTER);

        refreshButton = new JButton("Refresh");
        refreshButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "standard");
        refreshButton.addActionListener(_ -> loadDecks());
        topBar.add(refreshButton, BorderLayout.EAST);

        this.add(topBar, BorderLayout.NORTH);

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

        setupKonamiCodeListener();
        loadDecks();
    }

    private void setupKonamiCodeListener() {
        KeyEventDispatcher dispatcher = new KeyEventDispatcher() {
            private int index = 0;

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (e.getKeyCode() == KONAMI_CODE[index]) {
                        index++;
                        if (index == KONAMI_CODE.length) {
                            index = 0;
                            SwingUtilities.invokeLater(RepositoryPanel.this::showSecretMenu);
                        }
                    } else {
                        index = (e.getKeyCode() == KeyEvent.VK_UP) ? 1 : 0;
                    }
                }
                return false;
            }
        };

        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
                } else {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
                }
            }
        });
    }

    private void showSecretMenu() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        String currentUrl = remoteRepositoryService.getIndexUrl();

        Object result = JOptionPane.showInputDialog(
                parentWindow,
                "Developer Override Mode Unlocked!\nEnter a custom repository index URL:",
                "Secret Override Menu",
                JOptionPane.WARNING_MESSAGE,
                null,
                null,
                currentUrl
        );

        if (result != null) {
            String newUrl = result.toString().trim();
            if (!newUrl.isEmpty() && !newUrl.equals(currentUrl)) {
                log.info("Repository URL overridden: {}", newUrl);
                remoteRepositoryService.setIndexUrl(newUrl);

                searchField.setText("");
                loadDecks();
            }
        }
    }

    private void loadDecks() {
        if (refreshButton != null) {
            refreshButton.setEnabled(false);
            refreshButton.setText("Refreshing...");
        }

        gridContainer.removeAll();
        gridContainer.add(new JLabel("Loading avaiable decks..."));
        gridContainer.revalidate();
        gridContainer.repaint();

        new SwingWorker<List<RepositoryDeck>, Void>() {
            @Override
            protected List<RepositoryDeck> doInBackground() {
                try {
                    return remoteRepositoryService.fetchAvailabeDecks();
                } catch (Exception e) {
                    log.error("Error fetching available decks:", e);
                }
                return new ArrayList<>();
            }

            @Override
            protected void done() {
                if (refreshButton != null) {
                    refreshButton.setEnabled(true);
                    refreshButton.setText("Refresh");
                }

                try {
                    allRepositoryDecks = get();
                    filterDecks();
                } catch (Exception e) {
                    log.error("Failed to load repo decks", e);
                    gridContainer.removeAll();
                    gridContainer.add(new JLabel("Failed to load repository. Check network connection."));
                    gridContainer.revalidate();
                    gridContainer.repaint();
                }
            }
        }.execute();
    }

    private void filterDecks() {
        String query = searchField.getText().toLowerCase().trim();
        gridContainer.removeAll();

        List<RepositoryDeck> filtered = allRepositoryDecks.stream()
                .filter(deck -> query.isEmpty() || deck.getTitle().toLowerCase().contains(query) ||
                        (deck.getAuthor() != null && deck.getAuthor().toLowerCase().contains(query)) ||
                        (deck.getTags() != null && deck.getTags().stream().anyMatch(t -> t.toLowerCase().contains(query))))
                .toList();

        if (filtered.isEmpty()) {
            gridContainer.add(new JLabel("No decks found matching your search."));
        } else {
            for (RepositoryDeck repositoryDeck : filtered) {
                gridContainer.add(new RepositoryDeckWidget(repositoryDeck));
            }
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    private class RepositoryDeckWidget extends JPanel {
        public RepositoryDeckWidget(RepositoryDeck repositoryDeck) {
            this.setLayout(new BorderLayout(15, 15));
            this.putClientProperty(FlatClientProperties.STYLE_CLASS, "DeckWidget");

            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            JLabel titleLabel = new JLabel(repositoryDeck.getTitle());
            titleLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "titleLabel");
            infoPanel.add(titleLabel);
            infoPanel.add(Box.createVerticalStrut(5));

            JLabel authorLabel = new JLabel("By: " + (repositoryDeck.getAuthor() != null ? repositoryDeck.getAuthor() : "Unknown"));
            authorLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "subtitleLabel");
            infoPanel.add(authorLabel);
            infoPanel.add(Box.createVerticalStrut(5));

            //~~~
            JLabel descriptionLabel = new JLabel("<html>" + (repositoryDeck.getDescription() != null ? repositoryDeck.getDescription() : "") + "</html>");
            infoPanel.add(descriptionLabel);

            if (repositoryDeck.getTags() != null && !repositoryDeck.getTags().isEmpty()) {
                infoPanel.add(Box.createVerticalStrut(5));
                JLabel tagsLabel = new JLabel("Tags: " + String.join(", ", repositoryDeck.getTags()));
                tagsLabel.setForeground(Color.GRAY);
                infoPanel.add(tagsLabel);
            }

            this.add(infoPanel, BorderLayout.CENTER);

            boolean alreadyExists = existingDecks.stream()
                    .anyMatch(deck -> deck.getId().equals(repositoryDeck.getId()));
            JButton downloadButton = new JButton(alreadyExists ? "Already downloaded" : "Download & Import");
            if (alreadyExists) {
                downloadButton.setEnabled(false);
            }
            downloadButton.addActionListener(_ -> downloadDeck(repositoryDeck, downloadButton));
            this.add(downloadButton, BorderLayout.SOUTH);
        }

        private void downloadDeck(RepositoryDeck repositoryDeck, JButton button) {
            button.setEnabled(false);
            button.setText("Downloading...");

            new SwingWorker<Path, Void>() {
                @Override
                protected Path doInBackground() {
                    try {
                        return remoteRepositoryService.downloadDeck(repositoryDeck.getUrl());
                    } catch (Exception e) {
                        log.error("Error downloading deck: ", e);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    Window parentWindow = SwingUtilities.getWindowAncestor(RepositoryPanel.this);
                    try {
                        Path downloadedFile = get();
                        if (downloadedFile == null) return; // ~~
                        button.setText("Importing...");

                        Deck importedDeck = astraArchiveHandler.importAstraArchive(downloadedFile);
                        importedDeck.resetDeckProgress();

                        long sameNameCount = existingDecks.stream()
                                        .filter(deck -> !deck.getId().equals(importedDeck.getId()))
                                        .filter(deck -> deck.getTitle().equalsIgnoreCase(importedDeck.getTitle()))
                                        .count();
                        if (sameNameCount > 0) {
                            importedDeck.setTitle(importedDeck.getTitle() + " (" + (sameNameCount + 1) + ")");
                        }

                        eventBus.post(new DeckImportedEvent(importedDeck));
                        button.setText("Imported!");
                        JOptionPane.showMessageDialog(parentWindow,
                                "Successfully imported: " + importedDeck.getTitle());
                    } catch (Exception e) {
                        log.error("Failed to download or import repo deck", e);
                        button.setText("Download Failed");
                        button.setEnabled(true);
                        JOptionPane.showMessageDialog(parentWindow,
                                "Failed to download or import deck.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }
}
