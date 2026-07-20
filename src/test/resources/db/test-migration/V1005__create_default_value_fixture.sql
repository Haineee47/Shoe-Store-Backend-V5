CREATE TABLE default_value_fixtures
(
    id                  UUID         NOT NULL,
    version             BIGINT       NOT NULL,
    name                VARCHAR(100) NOT NULL,
    active              BOOLEAN      NOT NULL,
    status              VARCHAR(32)  NOT NULL,
    retry_count         INTEGER      NOT NULL,
    database_created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_default_value_fixtures
        PRIMARY KEY (id),

    CONSTRAINT ck_default_value_fixtures_name_not_blank
        CHECK (btrim(name) <> ''),

    CONSTRAINT ck_default_value_fixtures_status
        CHECK (status IN ('PENDING', 'ACTIVE')),

    CONSTRAINT ck_default_value_fixtures_retry_count_non_negative
        CHECK (retry_count >= 0)
);
