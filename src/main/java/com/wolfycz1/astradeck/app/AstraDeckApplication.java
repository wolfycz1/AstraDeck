package com.wolfycz1.astradeck.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.google.common.eventbus.EventBus;
import com.wolfycz1.astradeck.database.DatabaseConfig;
import com.wolfycz1.astradeck.database.DeckRepositoryService;
import com.wolfycz1.astradeck.storage.AstraArchiveHandler;
import com.wolfycz1.astradeck.storage.MediaStorageService;
import com.wolfycz1.astradeck.ui.MainFrame;
import com.wolfycz1.astradeck.ui.NavigationController;
import com.wolfycz1.astradeck.ui.util.ImageProvider;
import com.wolfycz1.astradeck.workers.MediaGarbageCollector;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import javax.swing.*;

@Slf4j
public class AstraDeckApplication {
    private EventBus eventBus;
    private DeckRepositoryService deckRepositoryService;
    private MediaStorageService mediaStorageService;
    private ImageProvider imageProvider;
    private AstraArchiveHandler astraArchiveHandler;
    private MediaGarbageCollector mediaGarbageCollector;

    public void start() {
        initializeFlatlaf();
        initializeComponents();
        registerShutdownHooks();
        launchUI();
    }

    private void initializeFlatlaf() {
        FlatLaf.registerCustomDefaultsSource("themes");
        FlatDarkLaf.setup();
    }

    private void initializeComponents() {
        eventBus = new EventBus();
        Jdbi jdbi = DatabaseConfig.initializeDatabase();

        deckRepositoryService = new DeckRepositoryService(jdbi);
        eventBus.register(deckRepositoryService);

        mediaStorageService = new MediaStorageService();
        imageProvider = new ImageProvider(mediaStorageService);
        astraArchiveHandler = new AstraArchiveHandler(mediaStorageService, imageProvider);

        mediaGarbageCollector = new MediaGarbageCollector(deckRepositoryService, mediaStorageService);
        mediaGarbageCollector.startDaemon();
    }

    private void registerShutdownHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down background daemon threads.");
            mediaGarbageCollector.shutdown();
        }, "AstraDeck-ShutdownHook"));
    }

    private void launchUI() {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            NavigationController navigationController = new NavigationController(eventBus, mainFrame,
                    mediaStorageService, imageProvider, astraArchiveHandler, deckRepositoryService);
            navigationController.start();
        });
    }
}
