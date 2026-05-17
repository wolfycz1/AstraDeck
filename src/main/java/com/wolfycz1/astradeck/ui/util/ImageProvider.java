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

@Slf4j
public class ImageProvider {
    private final MediaStorageService mediaStorageService;
    private final Map<String, ImageIcon> memoryCache;
    private final FlatSVGIcon missingIcon;

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

    public void preloadIcon(Media media, int maxWidth, int maxHeight) {
        getIcon(media, maxWidth, maxHeight);
    }

    public ImageIcon getIcon(Media media, int maxWidth, int maxHeight) {
        String cacheKey = media.name() + "_" + maxWidth + "x" + maxHeight;
        return memoryCache.computeIfAbsent(cacheKey, key -> loadFromDiskOrGenerate(media, key, maxWidth, maxHeight));
    }

    private ImageIcon loadFromDiskOrGenerate(Media media, String key, int maxWidth, int maxHeight) {
        File pngCache = OsPaths.CACHE_DIR.resolve(key + ".png").toFile();
        ImageIcon icon = attempDiskLoad(pngCache);
        if (icon != null) return icon;

        File jpgCache = OsPaths.CACHE_DIR.resolve(key + ".jpg").toFile();
        icon = attempDiskLoad(jpgCache);
        if (icon != null) return icon;

        return generateCacheProxy(media, key, maxWidth, maxHeight);
    }

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

    public void clearMemoryCache() {
        memoryCache.clear();
    }
}
