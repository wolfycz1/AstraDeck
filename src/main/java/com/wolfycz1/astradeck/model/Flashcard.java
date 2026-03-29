package com.wolfycz1.astradeck.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

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
public abstract class Flashcard {
    private UUID id = UUID.randomUUID();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
}
