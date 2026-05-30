package com.wolfycz1.astradeck.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.errorprone.annotations.MustBeClosed;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Base model for all flashcard types
 * @author wolfycz1
 */
@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextCard.class, name = "text"),
        @JsonSubTypes.Type(value = ImageCard.class, name = "image")
})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Flashcard {
    @EqualsAndHashCode.Include
    private UUID id = UUID.randomUUID();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    /**
     * Generates a short preview text representing the flashcard for the UI
     * @return short preview string
     */
    @JsonIgnore
    public abstract String getPreviewText();

    /**
     * Collects any media files attached to this card
     * @return stream of {@link Media} objects
     */
    @JsonIgnore
    @MustBeClosed
    public Stream<Media> getReferencedMedia() {
        return Stream.empty();
    }
}
