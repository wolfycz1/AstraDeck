package com.wolfycz1.astradeck.storage;

import com.wolfycz1.astradeck.model.Manifest;
import com.wolfycz1.astradeck.model.Media;
import com.wolfycz1.astradeck.util.OsPaths;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class MediaStorageService {
    private final Path persistentDir;
    private final Path cacheDir;

    public MediaStorageService() {
        this.persistentDir = OsPaths.DATA_DIR.resolve("media");
        this.cacheDir = OsPaths.CACHE_DIR;
        initializeDirectories();
    }

    private void initializeDirectories() {
        try {
            if (!Files.exists(persistentDir)) {
                Files.createDirectories(persistentDir);
                log.info("Created persistent media directory at: {}", persistentDir.toAbsolutePath());
            }
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
                log.info("Created local cache directory at: {}", cacheDir.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create AstraDeck storage directories", e);
        }
    }

    public void extractMedia(Path deckPath, Manifest manifest) throws IOException {
        log.info("Extracting media for deck: {}", manifest.getTitle());
        try (ZipFile zipFile = new ZipFile(deckPath.toFile())) {
            for (Media media : manifest.getMediaList()) {
                File targetFile = getMediaFile(media);
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
        return persistentDir.resolve(media.getFileName()).toFile();
    }

    public Media importLocalImage(File sourceFile) throws IOException, NoSuchAlgorithmException {
        String originalName = sourceFile.getName();
        String format = FilenameUtils.getExtension(sourceFile.getName()).toLowerCase();
        String hashName = generateHashName(sourceFile);

        Media media = new Media(hashName, "image", format, originalName);
        File destinationFile = getMediaFile(media);

        if (!destinationFile.exists()) {
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("Imported new persistent media: {}", media.getFileName());
        } else {
            log.info("Media already exists in persistent storage: {}", media.getFileName());
        }
        return media;
    }

    private String generateHashName(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        return HexFormat.of().formatHex(digest.digest(fileBytes));
    }

    public void garbageCollectMedia(Set<String> activeHashes) {
        log.info("Starting media garbage collection. Active DB references: {}", activeHashes.size());
        garbageCollectMedia(persistentDir, activeHashes, false);
        garbageCollectMedia(cacheDir, activeHashes, true);
        log.info("Media garbage collection complete.");
    }

    private void garbageCollectMedia(Path directory, Set<String> activeHashes, boolean isCacheDir) {
        if (!Files.exists(directory)) return;
        Instant now = Instant.now();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            int deletedCount = 0;
            for (Path file : stream) {
                if (Files.isDirectory(file)) continue;

                String fileName = file.getFileName().toString();
                String hash;

                if (isCacheDir) {
                    String baseName = FilenameUtils.getBaseName(fileName);
                    hash = baseName.contains("_") ? baseName.substring(0, baseName.indexOf('_')) : baseName;
                } else {
                    hash = FilenameUtils.getBaseName(fileName);
                }

                if (!activeHashes.contains(hash)) {
                    FileTime lastModified = Files.getLastModifiedTime(file);
                    Duration age = Duration.between(lastModified.toInstant(), now);
                    if (age.toMinutes() > 30) {
                        Files.deleteIfExists(file);
                        deletedCount++;
                        log.debug("Deleted unreferenced file: {}", file.getFileName());
                    } else {
                        log.debug("Retained unreferenced file {}, age: {}mins", file.getFileName(), age);
                    }
                }
            }

            if (deletedCount > 0) {
                log.info("Deleted {} orphaned files from {}", deletedCount, directory.getFileName());
            }
        } catch (IOException e) {
            log.error("Error during garbage collection in directory: {}", directory, e);
        }
    }
}
