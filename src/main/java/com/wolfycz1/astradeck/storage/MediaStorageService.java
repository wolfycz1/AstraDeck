package com.wolfycz1.astradeck.storage;

import com.wolfycz1.astradeck.model.Manifest;
import com.wolfycz1.astradeck.model.Media;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class MediaStorageService {
    private final Path tempDir;

    public MediaStorageService() {
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
            throw new IllegalStateException("Failed to create media temp directory", e);
        }
    }

    public void extractMedia(Path deckPath, Manifest manifest) throws IOException {
        log.info("Extracting media for deck: {}", manifest.getTitle());
        try (ZipFile zipFile = new ZipFile(deckPath.toFile())) {
            for (Media media : manifest.getMediaList()) {
                File targetFile = tempDir.resolve(media.getFileName()).toFile();
                if (!targetFile.exists()) {
                    ZipEntry entry = zipFile.getEntry(media.getPath());
                    if (entry != null) {
                        try (InputStream is = zipFile.getInputStream(entry)) {
                            FileUtils.copyInputStreamToFile(is, targetFile);
                        }
                    } else {
                        log.warn("Media file {} listed in manifest but missing from archive", media.getPath());
                    }
                }
            }
        }
    }

    public File getMediaFile(Media media) {
        return tempDir.resolve(media.getFileName()).toFile();
    }

    public Media importLocalImage(File sourceFile) throws IOException, NoSuchAlgorithmException {
        String originalName = sourceFile.getName();
        String format = FilenameUtils.getExtension(sourceFile.getName()).toLowerCase();
        String hashName = generateHashName(sourceFile);

        Media media = new Media(hashName, "image", format, originalName);
        File destinationFile = getMediaFile(media);

        if (!destinationFile.exists()) {
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return media;
    }

    private String generateHashName(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        return HexFormat.of().formatHex(digest.digest(fileBytes));
    }

    public void clearTemp() {
        try {
            if (Files.exists(tempDir)) {
                FileUtils.cleanDirectory(tempDir.toFile());
                log.info("Media temp cleared successfully.");
            }
        } catch (IOException e) {
            log.error("Failed to clear media temp: ", e);
        }
    }
}
