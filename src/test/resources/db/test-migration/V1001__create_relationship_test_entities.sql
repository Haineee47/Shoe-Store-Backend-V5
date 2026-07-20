CREATE TABLE relationship_parent_test_entities (
    id UUID NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_relationship_parent_test_entities
        PRIMARY KEY (id)
);

CREATE TABLE relationship_child_test_entities (
    id UUID NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    parent_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_relationship_child_test_entities
        PRIMARY KEY (id),

    CONSTRAINT fk_relationship_child_test_entities_parent
        FOREIGN KEY (parent_id)
        REFERENCES relationship_parent_test_entities (id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_relationship_child_test_entities_parent_id
    ON relationship_child_test_entities (parent_id);
