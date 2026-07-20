CREATE TABLE column_definition_test_entities (
    id UUID NOT NULL,
    version BIGINT NOT NULL,

    required_code VARCHAR(100) NOT NULL,
    optional_label VARCHAR(150),
    description TEXT,

    active BOOLEAN NOT NULL,
    quantity INTEGER NOT NULL,

    amount NUMERIC(19, 2) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_column_definition_test_entities
        PRIMARY KEY (id)
);
