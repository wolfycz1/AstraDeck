package com.wolfycz1.astradeck.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Represents the metadata of a deck listed on the remote repository
 * @author wolfycz1
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryDeck {
    private UUID id;
    private String title;
    private String author;
    private String description;
    private String url;
    private List<String> tags;
}
