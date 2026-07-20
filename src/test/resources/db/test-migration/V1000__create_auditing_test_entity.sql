CREATE TABLE auditing_test_entity
(
    id         UUID        NOT NULL,
    version    BIGINT      NOT NULL,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_auditing_test_entity
        PRIMARY KEY (id)
);
