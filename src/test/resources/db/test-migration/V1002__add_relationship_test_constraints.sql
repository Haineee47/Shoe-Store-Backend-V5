ALTER TABLE relationship_child_test_entities
    ADD COLUMN position INTEGER;

WITH ranked_children AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY parent_id
            ORDER BY created_at, id
        ) - 1 AS generated_position
    FROM relationship_child_test_entities
)
UPDATE relationship_child_test_entities child
SET position = ranked.generated_position
FROM ranked_children ranked
WHERE child.id = ranked.id;

ALTER TABLE relationship_child_test_entities
    ALTER COLUMN position SET NOT NULL;

ALTER TABLE relationship_child_test_entities
    ADD CONSTRAINT
        uk_relationship_children_parent_position
        UNIQUE (parent_id, position);

ALTER TABLE relationship_child_test_entities
    ADD CONSTRAINT
        ck_relationship_children_position_non_negative
        CHECK (position >= 0);

ALTER INDEX idx_relationship_child_test_entities_parent_id
    RENAME TO idx_relationship_children_parent_id;

ALTER TABLE relationship_child_test_entities
    RENAME CONSTRAINT
        fk_relationship_child_test_entities_parent
    TO
        fk_relationship_children_parent;
