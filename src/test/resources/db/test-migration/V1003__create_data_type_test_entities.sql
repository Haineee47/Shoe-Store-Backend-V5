CREATE TABLE data_type_test_entities (
    id UUID NOT NULL,
    version BIGINT NOT NULL,

    external_reference_id UUID NOT NULL,

    short_name VARCHAR(150) NOT NULL,
    description TEXT,

    active BOOLEAN NOT NULL,

    quantity INTEGER NOT NULL,
    view_count BIGINT NOT NULL,

    amount NUMERIC(19, 2) NOT NULL,
    weight_kg NUMERIC(12, 3) NOT NULL,

    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    business_date DATE NOT NULL,
    opening_time TIME WITHOUT TIME ZONE NOT NULL,

    status VARCHAR(50) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_data_type_test_entities
        PRIMARY KEY (id)
);
