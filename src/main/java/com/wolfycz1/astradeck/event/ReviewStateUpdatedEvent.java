package com.wolfycz1.astradeck.event;

import com.wolfycz1.astradeck.model.ReviewState;

/**
 * Signals that a review state has been updated
 * @param reviewState the updated {@link ReviewState} object
 * @author wolfycz1
 */
public record ReviewStateUpdatedEvent(ReviewState reviewState) {}
