package com.shoestore.shared.persistence.datatype;

import com.shoestore.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "data_type_test_entities")
public class DataTypeTestEntity extends AuditableEntity {

    @Column(
            name = "external_reference_id",
            nullable = false
    )
    private UUID externalReferenceId;

    @Column(
            name = "short_name",
            nullable = false,
            length = 150
    )
    private String shortName;

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
            name = "view_count",
            nullable = false
    )
    private long viewCount;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "weight_kg",
            nullable = false,
            precision = 12,
            scale = 3
    )
    private BigDecimal weightKg;

    @Column(
            name = "occurred_at",
            nullable = false
    )
    private Instant occurredAt;

    @Column(
            name = "business_date",
            nullable = false
    )
    private LocalDate businessDate;

    @Column(
            name = "opening_time",
            nullable = false
    )
    private LocalTime openingTime;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 50
    )
    private DataTypeTestStatus status;

    protected DataTypeTestEntity() {
    }

    public DataTypeTestEntity(
            UUID externalReferenceId,
            String shortName,
            String description,
            boolean active,
            int quantity,
            long viewCount,
            BigDecimal amount,
            BigDecimal weightKg,
            Instant occurredAt,
            LocalDate businessDate,
            LocalTime openingTime,
            DataTypeTestStatus status
    ) {
        this.externalReferenceId = externalReferenceId;
        this.shortName = shortName;
        this.description = description;
        this.active = active;
        this.quantity = quantity;
        this.viewCount = viewCount;
        this.amount = amount;
        this.weightKg = weightKg;
        this.occurredAt = occurredAt;
        this.businessDate = businessDate;
        this.openingTime = openingTime;
        this.status = status;
    }

    public UUID getExternalReferenceId() {
        return externalReferenceId;
    }

    public String getShortName() {
        return shortName;
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

    public long getViewCount() {
        return viewCount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public DataTypeTestStatus getStatus() {
        return status;
    }
}
