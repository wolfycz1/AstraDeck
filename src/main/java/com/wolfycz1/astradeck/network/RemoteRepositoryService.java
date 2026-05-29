package com.wolfycz1.astradeck.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wolfycz1.astradeck.model.RepositoryDeck;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * Handles network requests to fetch available decks and download them from a remote repository
 * @author wolfycz1
 */
@Slf4j
public class RemoteRepositoryService {
    @Getter
    @Setter
    private String indexUrl = "https://raw.githubusercontent.com/wolfycz1/AstraDeckDecks/main/index.json";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public RemoteRepositoryService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Reaches to the remote repository for a list of available decks
     * @return list of avaiable decks to download in form of {@link RepositoryDeck} objects
     */
    public List<RepositoryDeck> fetchAvailabeDecks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(indexUrl))
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch index. Status code: " + response.statusCode());
        }

        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    /**
     * Downloads a deck from the remote repository
     * @param downloadUrl url to the deck file
     * @return local file path of the temporarily saved deck
     */
    public Path downloadDeck(String downloadUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .GET().build();

        Path tempFile = Files.createTempFile("astradeck_download_", ".astra");
        tempFile.toFile().deleteOnExit();

        HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));

        if (response.statusCode() != 200) {
            throw new IOException("Download failed. Status code: " + response.statusCode());
        }

        return tempFile;
    }
}
