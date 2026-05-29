package com.wolfycz1.astradeck.storage.exceptions;

/**
 * Thrown when .astra archive is missing its components or has been corrupted
 * @author wolfycz1
 */
public class InvalidDeckException extends AstraArchiveException {
    public InvalidDeckException(String message) {
        super(message);
    }
}
