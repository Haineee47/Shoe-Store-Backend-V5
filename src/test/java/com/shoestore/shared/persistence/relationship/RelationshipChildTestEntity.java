package com.shoestore.shared.persistence.relationship;

import com.shoestore.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

@Entity
@Table(
        name = "relationship_child_test_entities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name =
                                "uk_relationship_children_parent_position",
                        columnNames = {
                                "parent_id",
                                "position"
                        }
                )
        },
        indexes = {
                @Index(
                        name =
                                "idx_relationship_children_parent_id",
                        columnList = "parent_id"
                )
        }
)
public class RelationshipChildTestEntity
        extends AuditableEntity {

    @Column(
            name = "name",
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            name = "position",
            nullable = false
    )
    private int position;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "parent_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name =
                            "fk_relationship_children_parent"
            )
    )
    private RelationshipParentTestEntity parent;

    protected RelationshipChildTestEntity() {
    }

    public RelationshipChildTestEntity(
            String name,
            int position
    ) {
        this.name = requireName(name);
        this.position = requirePosition(position);
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public RelationshipParentTestEntity getParent() {
        return parent;
    }

    void attachTo(RelationshipParentTestEntity parent) {
        Objects.requireNonNull(
                parent,
                "Parent must not be null"
        );

        if (this.parent != null && this.parent != parent) {
            throw new IllegalStateException(
                    "Child is already attached to another parent"
            );
        }

        this.parent = parent;
    }

    void detachFrom(RelationshipParentTestEntity parent) {
        if (this.parent == parent) {
            this.parent = null;
        }
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Name must not be blank"
            );
        }

        return name.trim();
    }

    private static int requirePosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException(
                    "Position must not be negative"
            );
        }

        return position;
    }
}
