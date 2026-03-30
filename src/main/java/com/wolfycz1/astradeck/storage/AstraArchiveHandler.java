package com.wolfycz1.astradeck.storage;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wolfycz1.astradeck.logic.MediaManager;
import com.wolfycz1.astradeck.model.Deck;
import com.wolfycz1.astradeck.model.Manifest;
import com.wolfycz1.astradeck.model.Media;
import com.wolfycz1.astradeck.storage.exceptions.AstraImportException;
import com.wolfycz1.astradeck.storage.exceptions.InvalidDeckException;
import com.wolfycz1.astradeck.storage.exceptions.MissingMediaException;
import com.wolfycz1.astradeck.storage.exceptions.UnsupportedVersionException;
import com.wolfycz1.astradeck.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AstraArchiveHandler {
    private final ObjectMapper mapper;

    public AstraArchiveHandler() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Deck importAstraArchive(Path filePath) throws AstraImportException, IOException {
        try (ZipFile zipFile = new ZipFile(filePath.toFile())) {
            ZipEntry manifestEntry = zipFile.getEntry("manifest.json");
            if (manifestEntry == null) {
                throw new InvalidDeckException("manifest.json missing. Invalid .astra file");
            }

            Manifest manifest;
            try (InputStream is = zipFile.getInputStream(manifestEntry)) {
                manifest = mapper.readValue(is, Manifest.class);
            }

            if (manifest.getVersion() > Constants.ASTRA_FORMAT_VERSION) {
                throw new UnsupportedVersionException(
                        "This deck requires a newer version of AstraDeck. " +
                                "Deck format: " + manifest.getVersion() +
                                ", App supports up to: " + Constants.ASTRA_FORMAT_VERSION
                );
            }

            for (Media media : manifest.getMediaList()) {
                ZipEntry mediaEntry = zipFile.getEntry(media.getPath());
                if (mediaEntry == null) {
                    throw new MissingMediaException("Corrupted archive: Missing " + media.type() + " file -> " + media.getPath());
                }
            }

            ZipEntry deckEntry = zipFile.getEntry("deck.json");
            if (deckEntry == null) {
                throw new InvalidDeckException("deck.json missing. Invalid .astra file");
            }

            try (InputStream is = zipFile.getInputStream(deckEntry)) {
                MediaManager mediaManager = new MediaManager();
                mediaManager.extractMedia(filePath, manifest);
                return mapper.readValue(is, Deck.class);
            }
        }
    }
}
