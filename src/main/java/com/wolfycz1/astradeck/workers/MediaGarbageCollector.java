package com.wolfycz1.astradeck.workers;

import com.wolfycz1.astradeck.database.DeckRepositoryService;
import com.wolfycz1.astradeck.storage.MediaStorageService;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MediaGarbageCollector {
    private final DeckRepositoryService deckRepositoryService;
    private final MediaStorageService mediaStorageService;
    private final ScheduledExecutorService scheduledExecutorService;

    public MediaGarbageCollector(DeckRepositoryService deckRepositoryService, MediaStorageService mediaStorageService) {
        this.deckRepositoryService = deckRepositoryService;
        this.mediaStorageService = mediaStorageService;

        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AstraDeck-MediaGC");
            t.setDaemon(true);
            return t;
        });
    }

    public void startDaemon() {
        log.info("Starting Media Garbage Collector Daemon.");
        scheduledExecutorService.scheduleAtFixedRate(this::runCollector, 1, 10, TimeUnit.MINUTES);
    }

    private void runCollector() {
        try {
            log.debug("Background cleanup awake.");
            Set<String> activeHashes = deckRepositoryService.getAllReferencedMediaHashes();
            mediaStorageService.garbageCollectMedia(activeHashes);
        } catch (Exception e) {
            log.error("Failed during routine background media sweep", e);
        }
    }

    public void shutdown() {
        scheduledExecutorService.shutdownNow();
    }
}
