package com.shoestore.shared.persistence.support;

import com.shoestore.shared.persistence.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "auditing_test_entity")
public class AuditingTestEntity extends AuditableEntity {

    @Column(
            name = "name",
            nullable = false,
            length = 100
    )
    private String name;

    protected AuditingTestEntity() {
    }

    public AuditingTestEntity(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Name must not be blank"
            );
        }

        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Name must not be blank"
            );
        }

        this.name = name.trim();
    }
}
