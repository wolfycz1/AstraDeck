package com.wolfycz1.astradeck.logic;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.wolfycz1.astradeck.model.Manifest;
import com.wolfycz1.astradeck.model.Media;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class MediaManager {
    private final Path tempDir;
    private final Map<String, ImageIcon> imageCache = new ConcurrentHashMap<>();

    public MediaManager() {
        this.tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "astradeck-temp");
        initializeTemp();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down JVM, triggering temp cleanup.");
            clearTemp();
        }));
    }

    private void initializeTemp() {
        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
                log.info("Created media temp directory at: {}", tempDir.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create media temp directory at " + tempDir.toAbsolutePath(), e);
        }
    }

    public void extractMedia(Path filePath, Manifest manifest) throws IOException {
        log.info("Extracting media for deck: {}", manifest.getTitle());

        try (ZipFile zipFile = new ZipFile(filePath.toFile())) {
            for (Media media : manifest.getMediaList()) {
                String mediaName = media.getFileName();
                String mediaPath = media.getPath();
                File targetFile = tempDir.resolve(mediaName).toFile();

                if (!targetFile.exists()) {
                    ZipEntry entry = zipFile.getEntry(mediaPath);
                    if (entry != null) {
                        try (InputStream is = zipFile.getInputStream(entry)) {
                            FileUtils.copyInputStreamToFile(is, targetFile);
                        }
                    } else {
                        log.warn("Media file {} listed in manifest but missing from archive", mediaPath);
                    }
                }
            }
        }
    }

    public File getMediaFile(Media media) {
        String fileName = media.getFileName();
        return tempDir.resolve(fileName).toFile();
    }

    public ImageIcon getImageIcon(Media media, int maxWidth, int maxHeight) {
        String cacheKey = media.getFileName() + "_" + maxWidth + "x" + maxHeight;
        if (imageCache.containsKey(cacheKey)) {
            return imageCache.get(cacheKey);
        }

        File file = getMediaFile(media);
        if (!file.exists()) {
            log.error("Image not found in temp: {}", file.getPath());
            return new FlatSVGIcon("icons/missing.svg");
        }

        ImageIcon original = new ImageIcon(file.getAbsolutePath());
        int originalWidth = original.getIconWidth();
        int originalHeight = original.getIconHeight();

        ImageIcon imageIcon;
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            imageIcon = original;
        } else {
            double ratio = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
            int newWidth = (int) (originalWidth * ratio);
            int newHeight = (int) (originalHeight * ratio);
            Image scaled = original.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(scaled);
        }

        imageCache.put(cacheKey, imageIcon);
        return imageIcon;
    }

    public void clearTemp() {
        try {
            imageCache.clear();
            if (Files.exists(tempDir)) {
                FileUtils.cleanDirectory(tempDir.toFile());
                log.info("Media temp cleared successfully.");
            }
        } catch (IOException e) {
            log.error("Failed to clear media temp: ", e);
        }
    }
}
