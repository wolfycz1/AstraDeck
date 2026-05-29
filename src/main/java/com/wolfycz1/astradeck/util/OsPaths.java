package com.wolfycz1.astradeck.util;

import dev.dirs.BaseDirectories;

import java.nio.file.Path;

/**
 * Resolves the correct OS-specific file paths for the application's data and cache directories
 * @author wolfycz1
 */
public class OsPaths {
    /** Used for persistent storage **/
    public static final Path DATA_DIR;
    /** Used for temporary storage **/
    public static final Path CACHE_DIR;

    static {
        BaseDirectories baseDirectories = BaseDirectories.get();
        DATA_DIR = Path.of(baseDirectories.dataDir, Constants.APP_NAME);
        CACHE_DIR = Path.of(baseDirectories.cacheDir, Constants.APP_NAME);
    }
}
