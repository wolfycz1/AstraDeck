package com.wolfycz1.astradeck.storage.exceptions;

/**
 * Base exception for errors that occur while processing .astra archives
 * @author wolfycz1
 */
public class AstraArchiveException extends Exception {
    public AstraArchiveException(String message) {
        super(message);
    }
}

