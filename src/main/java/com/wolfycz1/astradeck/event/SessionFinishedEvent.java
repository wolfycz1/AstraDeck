package com.wolfycz1.astradeck.event;

/**
 * Signals that a study session has been finished
 * @param totalReviewed amount of cards reviewed this session
 * @author wolfycz1
 */
public record SessionFinishedEvent(int totalReviewed) {}
