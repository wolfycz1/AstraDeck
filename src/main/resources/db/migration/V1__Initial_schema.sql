CREATE TABLE decks (
    id TEXT PRIMARY KEY,
    title TEXT,
    author TEXT,
    description TEXT,
    languages TEXT,
    created_at INTEGER,
    updated_at INTEGER
);

CREATE TABLE flashcards (
    id TEXT PRIMARY KEY,
    deck_id TEXT,
    created_at INTEGER,
    updated_at INTEGER,
    data TEXT,
    FOREIGN KEY(deck_id) REFERENCES decks(id) ON DELETE CASCADE
);

CREATE TABLE review_states (
    card_id TEXT PRIMARY KEY,
    last_review_date INTEGER,
    next_review_date INTEGER,
    repetitions INTEGER,
    interval INTEGER,
    ease_factor REAL,
    FOREIGN KEY(card_id) REFERENCES flashcards(id) ON DELETE CASCADE
);