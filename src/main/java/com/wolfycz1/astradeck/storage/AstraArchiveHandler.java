package com.wolfycz1.astradeck.storage;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wolfycz1.astradeck.logic.MediaManager;
import com.wolfycz1.astradeck.model.Deck;
import com.wolfycz1.astradeck.model.Manifest;
import com.wolfycz1.astradeck.model.Media;
import com.wolfycz1.astradeck.storage.exceptions.AstraArchiveException;
import com.wolfycz1.astradeck.storage.exceptions.InvalidDeckException;
import com.wolfycz1.astradeck.storage.exceptions.MissingMediaException;
import com.wolfycz1.astradeck.storage.exceptions.UnsupportedVersionException;
import com.wolfycz1.astradeck.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Slf4j
public class AstraArchiveHandler {
    private final ObjectMapper mapper;
    private final MediaManager mediaManager;

    public AstraArchiveHandler(MediaManager mediaManager) {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.mediaManager = mediaManager;

        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    }

    public Deck importAstraArchive(Path filePath) throws AstraArchiveException, IOException {
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
                mediaManager.extractMedia(filePath, manifest);

                CompletableFuture.runAsync(() -> {
                    log.info("Starting background image scale");
                    for (Media media : manifest.getMediaList()) {
                        mediaManager.getImageIcon(media, Constants.MAX_CARD_IMAGE_WIDTH, Constants.MAX_CARD_IMAGE_HEIGHT);
                    }
                    log.info("Background image scale cached.");
                });

                return mapper.readValue(is, Deck.class);
            }
        }
    }

    public void exportAstraArchive(Deck deck, Path destination) throws AstraArchiveException, IOException {
        Manifest manifest = ManifestManager.generateManifest(deck);

        try (FileOutputStream fos = new FileOutputStream(destination.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            zos.putNextEntry(new ZipEntry("manifest.json"));
            mapper.writeValue(zos, manifest);
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("deck.json"));
            mapper.writeValue(zos, deck);
            zos.closeEntry();

            for (Media media : manifest.getMediaList()) {
                File tempFile = mediaManager.getMediaFile(media);
                if (tempFile.exists()) {
                    zos.putNextEntry(new ZipEntry(media.getPath()));

                    try (FileInputStream fis = new FileInputStream(tempFile)) {
                        IOUtils.copy(fis, zos);
                    }
                    zos.closeEntry();
                } else {
                    throw new MissingMediaException("Cannot export deck. Missing media file in cache: " + media.getPath());
                }
            }
            log.info("Deck exported to: {}", destination);
        }
    }
}
