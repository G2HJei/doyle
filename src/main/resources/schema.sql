CREATE TABLE event_outcome
(
    id              UUID PRIMARY KEY,
    event_id        UUID         NOT NULL UNIQUE,
    event_name      VARCHAR(255) NOT NULL,
    event_winner_id UUID         NOT NULL,
    published       BOOLEAN      NOT NULL,
    version         INT          NOT NULL
);
CREATE INDEX event_outcome_published_id_idx ON event_outcome (published, id);

CREATE TABLE bet
(
    id                    UUID PRIMARY KEY,
    user_id               UUID           NOT NULL,
    event_id              UUID           NOT NULL,
    event_market_id       UUID           NOT NULL,
    event_winner_id       UUID           NOT NULL,
    amount                DECIMAL(19, 2) NOT NULL,
    status                VARCHAR(16)    NOT NULL,
    result                VARCHAR(16),
    settled_event_version INT,
    version               INT            NOT NULL
);
CREATE INDEX bet_event_id_id_idx ON bet (event_id, id);

CREATE TABLE bet_settlement
(
    id                    UUID PRIMARY KEY,
    bet_id                UUID           NOT NULL REFERENCES bet (id),
    delta_amount          DECIMAL(19, 2) NOT NULL,
    payout_status         VARCHAR(16)    NOT NULL,
    settled_event_version INT            NOT NULL,
    UNIQUE (bet_id, settled_event_version)
);

CREATE TABLE settlement_job
(
    id              UUID PRIMARY KEY,
    event_id        UUID    NOT NULL,
    outcome_version INT     NOT NULL,
    last_bet_id     UUID,
    completed       BOOLEAN NOT NULL,
    version         INT     NOT NULL,
    UNIQUE (event_id, outcome_version)
);
CREATE INDEX settlement_job_completed_id_idx ON settlement_job (completed, id);
