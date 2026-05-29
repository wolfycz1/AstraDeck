package com.wolfycz1.astradeck.storage.exceptions;

/**
 * Thrown when attempting to open a deck that was created with a newer incompatible version of the app
 * @author wolfycz1
 */
public class UnsupportedVersionException extends AstraArchiveException {
    public UnsupportedVersionException(String message) {
        super(message);
    }
}
