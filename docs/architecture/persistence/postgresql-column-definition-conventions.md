# PostgreSQL Column Definition Conventions

## 1. Purpose

This document defines how PostgreSQL columns are represented consistently
across Java fields, JPA mappings, Flyway migrations, and PostgreSQL metadata.

## 2. Source of Truth

Flyway migrations are the source of truth for database column definitions.

JPA annotations must describe mappings consistently with Flyway.

Hibernate schema generation must not replace Flyway.

## 3. Column Names

Every persistent field must declare an explicit column name.

Column names use lowercase snake_case.

Example:


@Column(name = "display_name")
private String displayName;

Migration:

display_name VARCHAR(150)
4. Nullability

Required columns must declare:

nullable = false

and must match:

NOT NULL

Optional columns should omit nullable = true.

Primitive fields must explicitly declare nullable = false.

5. String Length

Every VARCHAR mapping must declare an intentional length.

Example:

@Column(
        name = "code",
        nullable = false,
        length = 100
)
private String code;

Migration:

code VARCHAR(100) NOT NULL

Do not rely on the default length of 255.

6. Numeric Precision and Scale

Every BigDecimal mapping must define explicit precision and scale.

Example:

@Column(
        name = "amount",
        nullable = false,
        precision = 19,
        scale = 2
)
private BigDecimal amount;

Migration:

amount NUMERIC(19, 2) NOT NULL
7. Column Definition

Use columnDefinition only when standard JPA attributes do not express the
required mapping.

The current baseline permits it for long text:

@Column(
        name = "description",
        columnDefinition = "TEXT"
)
private String description;

Do not place nullability, default values, or standard varchar/numeric
definitions inside columnDefinition.

8. Default Values

Database defaults must be defined in Flyway.

Application defaults must be defined in Java.

Avoid defining conflicting defaults in both locations.

Do not place database defaults inside JPA columnDefinition.

9. Insertable and Updatable

Do not declare insertable = true or updatable = true, because they are the
defaults.

Use insertable = false or updatable = false only for deliberate
database-owned or immutable mappings.

10. Consistency Rules

The following must match:

column name;
nullability;
varchar length;
numeric precision;
numeric scale;
temporal type;
database default ownership.
11. Prohibited Patterns

Do not rely on implicit varchar length:

@Column(name = "code")
private String code;

Do not omit numeric precision and scale:

@Column(name = "amount")
private BigDecimal amount;

Do not embed ordinary DDL in columnDefinition:

@Column(
        name = "name",
        columnDefinition = "VARCHAR(150) NOT NULL"
)

Do not define SQL defaults in entity mappings:

@Column(
        name = "active",
        columnDefinition = "BOOLEAN DEFAULT TRUE"
)
12. Review Checklist
Every persistent field has an explicit column name.
Required columns use nullable = false.
Primitive fields use nullable = false.
Every varchar field has an explicit length.
Every decimal field has precision and scale.
columnDefinition is limited to justified cases.
Default ownership is clear.
Flyway and JPA definitions match.
