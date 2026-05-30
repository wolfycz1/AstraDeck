package com.wolfycz1.astradeck.ui.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wolfycz1.astradeck.model.Media;
import com.wolfycz1.astradeck.storage.MediaStorageService;
import com.wolfycz1.astradeck.util.OsPaths;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Provider for cached scaled images for the UI
 * @author wolfycz1
 */
@Slf4j
public class ImageProvider {
    private final MediaStorageService mediaStorageService;
    private final Cache<String, ImageIcon> memoryCache;
    private final FlatSVGIcon missingIcon;

    /**
     * Constructs the LRU cache
     */
    public ImageProvider(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;

        long maxCacheBytes = 100L * 1024L * 1024L;
        this.memoryCache = Caffeine.newBuilder()
                .maximumWeight(maxCacheBytes)
                .weigher((String _, ImageIcon icon) -> {
                    if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) return 1;
                    return icon.getIconWidth() * icon.getIconHeight() * 4;
                })
                .build();

        this.missingIcon = new FlatSVGIcon("icons/missing.svg");
    }

    /**
     * Preloads a scaled icon to the cache asynchronously
     * @param media image to scale
     * @param maxWidth max width to scale it to
     * @param maxHeight max height to scale it to
     */
    public void preloadIcon(Media media, int maxWidth, int maxHeight) {
        CompletableFuture.runAsync(() -> getIcon(media, maxWidth, maxHeight));
    }

    /**
     * Tries to retrieve the image from the memory cache, trying to load from disk if absent.
     * If that fails, it falls back to regenerating the image
     * @param media image to scale
     * @param maxWidth max width to scale it to
     * @param maxHeight max height to scale it to
     * @return the icon
     */
    public ImageIcon getIcon(Media media, int maxWidth, int maxHeight) {
        String cacheKey = media.name() + "_" + maxWidth + "x" + maxHeight;

        return memoryCache.get(cacheKey, key -> {
            ImageIcon icon = loadFromDiskOrGenerate(media, key, maxWidth, maxHeight);
            return icon != null ? icon : missingIcon;
        });
    }

    /**
     * Tries loading the image from disk, if that fails, resorts to regenerating the image.
     * @param media image to load
     * @param key the cache key
     * @param maxWidth max width to scale it to
     * @param maxHeight max height to scale it to
     * @return the icon
     */
    private ImageIcon loadFromDiskOrGenerate(Media media, String key, int maxWidth, int maxHeight) {
        File pngCache = OsPaths.CACHE_DIR.resolve(key + ".png").toFile();
        ImageIcon icon = attemptDiskLoad(pngCache);
        if (icon != null) return icon;

        File jpgCache = OsPaths.CACHE_DIR.resolve(key + ".jpg").toFile();
        icon = attemptDiskLoad(jpgCache);
        if (icon != null) return icon;

        return generateCacheProxy(media, key, maxWidth, maxHeight);
    }

    /**
     * Attempts to load a file from the disk
     * @param cacheFile file to attempt to load
     * @return the file, or {@code null} if it couldn't be loaded
     */
    private ImageIcon attemptDiskLoad(File cacheFile) {
        if (cacheFile.exists()) {
            try {
                BufferedImage image = ImageIO.read(cacheFile);
                if (image != null) return new ImageIcon(image);
            } catch (IOException e) {
                log.warn("Corrupted cache file, regenerating: {}", cacheFile.getName());
            }
        }
        return null;
    }

    /**
     * Tries regenerating the image from the original media, if it can't, it returns the missing image icon
     * @param media media to load and scale
     * @param key the cache key
     * @param maxWidth max width to scale it to
     * @param maxHeight max height to scale it to
     * @return the icon
     */
    private ImageIcon generateCacheProxy(Media media, String key, int maxWidth, int maxHeight) {
        File original = mediaStorageService.getMediaFile(media);
        if (!original.exists()) {
            log.error("Original image missing from persistent storage: {}", original.getPath());
            return null;
        }

        try {
            BufferedImage image = ImageIO.read(original);
            if (image == null) return null;

            BufferedImage scaledImage;
            boolean hasTransparency = image.getColorModel().hasAlpha();

            if (image.getWidth() <= maxWidth && image.getHeight() <= maxHeight) {
                scaledImage = image;
            } else {
                scaledImage = Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, maxWidth, maxHeight);
            }

            String ext = hasTransparency ? "png" : "jpg";
            File cachedProxy = OsPaths.CACHE_DIR.resolve(key + "." + ext).toFile();
            ImageIO.write(scaledImage, ext, cachedProxy);

            if (scaledImage != image) {
                image.flush();
            }
            return new ImageIcon(scaledImage);
        } catch (IOException e) {
            log.error("Failed to generate image proxy for: {}", original.getName(), e);
            return null;
        }
    }

    /** Clears all stored images in memory **/
    public void clearMemoryCache() {
        memoryCache.invalidateAll();
    }
}
