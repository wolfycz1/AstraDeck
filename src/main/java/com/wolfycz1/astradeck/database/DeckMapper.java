package com.wolfycz1.astradeck.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wolfycz1.astradeck.model.Deck;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
public class DeckMapper implements RowMapper<Deck> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Deck map(ResultSet rs, StatementContext ctx) throws SQLException {
        Deck deck = new Deck();
        deck.setId(UUID.fromString(rs.getString("id")));
        deck.setTitle(rs.getString("title"));
        deck.setAuthor(rs.getString("author"));
        deck.setDescription(rs.getString("description"));

        String languages = rs.getString("languages");
        if (languages != null && !languages.isBlank()) {
            try {
                deck.setLanguages(mapper.readValue(languages, new TypeReference<>() {}));
            } catch (Exception e) {
                log.warn("Value {} couldn't be parsed.", languages);
                deck.setLanguages(new ArrayList<>());
            }
        }

        deck.setCreatedAt(MapperUtil.parseInstant(rs.getObject("created_at")));
        deck.setUpdatedAt(MapperUtil.parseInstant(rs.getObject("updated_at")));
        return deck;
    }
}
