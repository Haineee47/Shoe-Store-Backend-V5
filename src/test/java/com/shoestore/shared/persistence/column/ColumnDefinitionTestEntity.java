package com.shoestore.shared.persistence.column;

import com.shoestore.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "column_definition_test_entities")
public class ColumnDefinitionTestEntity
        extends AuditableEntity {

    @Column(
            name = "required_code",
            nullable = false,
            length = 100
    )
    private String requiredCode;

    @Column(
            name = "optional_label",
            length = 150
    )
    private String optionalLabel;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "active",
            nullable = false
    )
    private boolean active;

    @Column(
            name = "quantity",
            nullable = false
    )
    private int quantity;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    protected ColumnDefinitionTestEntity() {
    }

    public ColumnDefinitionTestEntity(
            String requiredCode,
            String optionalLabel,
            String description,
            boolean active,
            int quantity,
            BigDecimal amount
    ) {
        this.requiredCode = requiredCode;
        this.optionalLabel = optionalLabel;
        this.description = description;
        this.active = active;
        this.quantity = quantity;
        this.amount = amount;
    }

    public String getRequiredCode() {
        return requiredCode;
    }

    public String getOptionalLabel() {
        return optionalLabel;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
