package com.wolfycz1.astradeck.ui.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provider for cached scaled images for the UI
 * @author wolfycz1
 */
@Slf4j
public class ImageProvider {
    private final MediaStorageService mediaStorageService;
    private final Map<String, ImageIcon> memoryCache;
    private final FlatSVGIcon missingIcon;

    /**
     * Constructs the LRU cache
     */
    public ImageProvider(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
        this.memoryCache = Collections.synchronizedMap(
                new LinkedHashMap<>(100, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, ImageIcon> eldest) {
                        return size() > 200;
                    }
                }
        );
        this.missingIcon = new FlatSVGIcon("icons/missing.svg");
    }

    /**
     * Preloads a scaled icon to the cache
     * @param media image to scale
     * @param maxWidth max width to scale it to
     * @param maxHeight max height to scale it to
     */
    public void preloadIcon(Media media, int maxWidth, int maxHeight) {
        getIcon(media, maxWidth, maxHeight);
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
        return memoryCache.computeIfAbsent(cacheKey, key -> loadFromDiskOrGenerate(media, key, maxWidth, maxHeight));
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
        ImageIcon icon = attempDiskLoad(pngCache);
        if (icon != null) return icon;

        File jpgCache = OsPaths.CACHE_DIR.resolve(key + ".jpg").toFile();
        icon = attempDiskLoad(jpgCache);
        if (icon != null) return icon;

        return generateCacheProxy(media, key, maxWidth, maxHeight);
    }

    /**
     * Attempts to load a file from the disk
     * @param cacheFile file to attempt to load
     * @return the file, or {@code null} if it couldn't be loaded
     */
    private ImageIcon attempDiskLoad(File cacheFile) {
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
            return missingIcon;
        }

        try {
            BufferedImage image = ImageIO.read(original);
            if (image == null) return missingIcon;

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
            return missingIcon;
        }
    }

    /** Clears all stored images in memory **/
    public void clearMemoryCache() {
        memoryCache.clear();
    }
}
