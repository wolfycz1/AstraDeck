package com.wolfycz1.astradeck.util;

import dev.dirs.BaseDirectories;

import java.nio.file.Path;

public class OsPaths {
    public static final Path DATA_DIR;
    public static final Path CACHE_DIR;

    static {
        BaseDirectories baseDirectories = BaseDirectories.get();
        DATA_DIR = Path.of(baseDirectories.dataDir, Constants.APP_NAME);
        CACHE_DIR = Path.of(baseDirectories.cacheDir, Constants.APP_NAME);
    }
}
