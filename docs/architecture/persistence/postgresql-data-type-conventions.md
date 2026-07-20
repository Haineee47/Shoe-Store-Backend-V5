# PostgreSQL Data-Type Conventions

## 1. Purpose

This document defines the PostgreSQL data-type conventions for the Shoe Store
Backend.

The goals are:

- Keep Java, Hibernate, Flyway, and PostgreSQL mappings consistent.
- Avoid lossy or ambiguous data representations.
- Preserve monetary precision.
- Standardize date and time handling.
- Keep schema definitions understandable and portable.
- Prevent accidental reliance on Hibernate schema generation.

## 2. Source of Truth

Flyway migrations are the source of truth for PostgreSQL column types.

JPA annotations describe entity mappings and support Hibernate schema
validation.

Hibernate must not generate or mutate the production schema.

## 3. General Rules

Every persistent field must use a data type that reflects its business meaning.

Do not choose a database type only because it is convenient in Java.

The following must be explicit where applicable:

- string length;
- numeric precision;
- numeric scale;
- nullability;
- enum storage;
- temporal semantics.

## 4. UUID

Java type:

UUID

PostgreSQL type:

UUID

Example:

@Column(
        name = "external_reference_id",
        nullable = false
)
private UUID externalReferenceId;

Migration:

external_reference_id UUID NOT NULL

Do not store UUID values as VARCHAR or CHAR.

Do not introduce sequential numeric identifiers without an approved decision.

1. String Types

Use VARCHAR(n) when a meaningful maximum length exists.

Examples:

name        VARCHAR(150)
email       VARCHAR(320)
sku         VARCHAR(100)
status      VARCHAR(50)

JPA example:

@Column(
        name = "name",
        nullable = false,
        length = 150
)
private String name;

Use TEXT for genuinely long-form content.

Examples:

description
notes
failure_details

JPA example:

@Column(
        name = "description",
        columnDefinition = "TEXT"
)
private String description;

Do not use TEXT for every string.

Do not use CHAR for ordinary business strings.

6. Boolean

Java type:

boolean

or:

Boolean

PostgreSQL type:

BOOLEAN

Use primitive boolean when null is not a valid state.

Use Boolean only when three states are intentionally required:

true
false
unknown

Example:

@Column(
        name = "active",
        nullable = false
)
private boolean active;

Migration:

active BOOLEAN NOT NULL
7. Integer Numbers

Java:

int / Integer

PostgreSQL:

INTEGER

Use for bounded counts such as:

quantity
position
retry_count

Java:

long / Long

PostgreSQL:

BIGINT

Use when the expected range may exceed INTEGER.

Do not use BIGINT automatically for every count.

8. Monetary Values

Java type:

BigDecimal

PostgreSQL type:

NUMERIC(19, 2)

JPA example:

@Column(
        name = "unit_price",
        nullable = false,
        precision = 19,
        scale = 2
)
private BigDecimal unitPrice;

Migration:

unit_price NUMERIC(19, 2) NOT NULL

Do not use:

float
double
REAL
DOUBLE PRECISION
MONEY

for monetary values.

9. Decimal Measurements

Use BigDecimal and explicit precision and scale.

Examples:

weight_kg        NUMERIC(12, 3)
discount_rate    NUMERIC(5, 2)
tax_rate         NUMERIC(5, 2)

The precision and scale must reflect the business domain.

Do not reuse NUMERIC(19, 2) automatically for every decimal value.

10. Instant

Use Instant for an absolute point on the timeline.

PostgreSQL type:

TIMESTAMP WITH TIME ZONE

JPA example:

@Column(
        name = "occurred_at",
        nullable = false
)
private Instant occurredAt;

Migration:

occurred_at TIMESTAMP WITH TIME ZONE NOT NULL

Suitable fields include:

created_at
updated_at
deleted_at
confirmed_at
paid_at
expires_at

Store and process application timestamps in UTC.

11. LocalDate

Use LocalDate for a calendar date without time or timezone.

PostgreSQL type:

DATE

Examples:

business_date
date_of_birth
promotion_start_date
promotion_end_date

Do not use timestamp types when only a date is meaningful.

12. LocalTime

Use LocalTime for a time of day without a date or timezone.

PostgreSQL type:

TIME WITHOUT TIME ZONE

Examples:

store_opening_time
store_closing_time

Do not use LocalTime for event timestamps.

13. LocalDateTime

LocalDateTime must not be the default timestamp type.

It may be used only when the business value intentionally has no timezone.

Any new LocalDateTime persistent field requires explicit review.

For system events, use Instant.

14. Enum

Enums must use:

@Enumerated(EnumType.STRING)

Example:

@Enumerated(EnumType.STRING)
@Column(
        name = "status",
        nullable = false,
        length = 50
)
private TestStatus status;

Migration:

status VARCHAR(50) NOT NULL

Do not use:

EnumType.ORDINAL

Do not use PostgreSQL native enum types in the current baseline.

15. Binary Data

Java:

byte[]

PostgreSQL:

BYTEA

Use binary columns only for small binary values that belong in the database.

Large documents and product images should normally be stored outside the
database, with only metadata or object-storage references persisted.

16. JSON and Array Types

The current baseline does not introduce:

JSON
JSONB
ARRAY

Use normalized relational columns and tables by default.

Introducing PostgreSQL-specific document or array types requires:

a concrete business use case;
query requirements;
an approved architecture decision;
explicit Flyway mappings;
dedicated integration tests.
17. PostgreSQL-Specific Numeric Types

Do not use:

MONEY
SERIAL
BIGSERIAL

The current identifier baseline is UUID.

Monetary values use NUMERIC.

18. Nullability

Java wrapper types do not automatically define business nullability.

Database nullability must be explicit in Flyway.

JPA nullability should match Flyway.

Example:

@Column(
        name = "quantity",
        nullable = false
)
private int quantity;

Migration:

quantity INTEGER NOT NULL

Optional values must use nullable Java types.

Example:

private BigDecimal discountAmount;
19. Default Values

Database defaults should be used only when the database owns the default.

Application-owned defaults should be established in Java.

Avoid defining conflicting defaults in both Java and PostgreSQL.

When a database default exists, document why direct database writers need it.

20. Mapping Consistency

The following must remain aligned:

Java field type;
JPA annotation;
Flyway column type;
business validation;
database constraints.

Example:

@Column(
        name = "amount",
        nullable = false,
        precision = 19,
        scale = 2
)
private BigDecimal amount;

must match:

amount NUMERIC(19, 2) NOT NULL
21. Prohibited Patterns

Money stored as floating point:

private double price;

Enum stored by ordinal:

@Enumerated(EnumType.ORDINAL)

UUID stored as text:

id VARCHAR(36)

System timestamp without timezone:

created_at TIMESTAMP WITHOUT TIME ZONE

Unbounded decimal mapping:

private BigDecimal amount;

without explicit precision and scale.

Using TEXT for every string field.

Using LocalDateTime by default for audit timestamps.

22. Review Checklist

Every persistent field should verify:

Java type reflects business meaning.
Flyway type matches the Java mapping.
Strings have an intentional length or use TEXT.
Money uses BigDecimal and NUMERIC(19, 2).
Other decimals use explicit precision and scale.
Absolute timestamps use Instant.
Instant maps to TIMESTAMP WITH TIME ZONE.
Date-only values use LocalDate.
Enums use EnumType.STRING.
UUID values use PostgreSQL UUID.
Nullability matches between entity and migration.
PostgreSQL-specific types are not introduced without approval.
