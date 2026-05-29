package com.wolfycz1.astradeck.storage.exceptions;

/**
 * Thrown when a media file referenced by the deck cannot be found
 * @author wolfycz1
 */
public class MissingMediaException extends AstraArchiveException {
    public MissingMediaException(String message) {
        super(message);
    }
}
