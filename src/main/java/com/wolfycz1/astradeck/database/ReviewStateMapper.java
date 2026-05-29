package com.wolfycz1.astradeck.database;

import com.wolfycz1.astradeck.model.ReviewState;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Maps database rows from review states table to review state objects
 * @author woflycz1
 */
public class ReviewStateMapper implements RowMapper<ReviewState> {

    /**
     * Converts a signle database row into a {@link ReviewState} object
     * @param rs {@link ResultSet} containing row data
     * @param ctx Jdbi {@link StatementContext} containing execution context and configuration
     * @return the created {@link ReviewState} object
     * @throws SQLException error thrown by the database
     */
    @Override
    public ReviewState map(ResultSet rs, StatementContext ctx) throws SQLException {
        ReviewState reviewState = new ReviewState();
        reviewState.setCardId(UUID.fromString(rs.getString("card_id")));
        reviewState.setLastReviewDate(MapperUtil.parseInstant(rs.getObject("last_review_date")));
        reviewState.setNextReviewDate(MapperUtil.parseInstant(rs.getObject("next_review_date")));
        reviewState.setRepetitions(rs.getInt("repetitions"));
        reviewState.setInterval(rs.getInt("interval"));
        reviewState.setEaseFactor(rs.getDouble("ease_factor"));

        return reviewState;
    }
}
