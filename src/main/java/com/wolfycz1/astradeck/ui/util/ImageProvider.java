package com.wolfycz1.astradeck.ui.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.wolfycz1.astradeck.model.Media;
import com.wolfycz1.astradeck.storage.MediaStorageService;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ImageProvider {
    private final MediaStorageService mediaStorageService;
    private final Map<String, ImageIcon> imageCache;
    private final FlatSVGIcon missingIcon;

    public ImageProvider(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
        this.imageCache = new ConcurrentHashMap<>();
        this.missingIcon = new FlatSVGIcon("icons/missing.svg");
    }

    public void preloadIcon(Media media, int maxWidth, int maxHeight) {
        getIcon(media, maxWidth, maxHeight);
    }

    public ImageIcon getIcon(Media media, int maxWidth, int maxHeight) {
        String cacheKey = media.getFileName() + "_" + maxWidth + "x" + maxHeight;
        return imageCache.computeIfAbsent(cacheKey, _ -> loadAndScale(media, maxWidth, maxHeight));
    }

    private ImageIcon loadAndScale(Media media, int maxWidth, int maxHeight) {
        File file = mediaStorageService.getMediaFile(media);
        if (!file.exists()) {
            log.error("Image not found in temp: {}", file.getPath());
            return missingIcon;
        }

        ImageIcon original = new ImageIcon(file.getAbsolutePath());
        int originalWidth = original.getIconWidth();
        int originalHeight = original.getIconHeight();

        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return original;
        }

        double ratio = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        Image scaled = original.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    public void clearCache() {
        imageCache.clear();
    }
}
