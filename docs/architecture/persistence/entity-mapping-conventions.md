# Entity and Column Mapping Conventions

## 1. Purpose

This document defines JPA entity and column mapping conventions for the
Shoe Store Backend.

The goals are:

- Keep entity mappings explicit and predictable.
- Align Java models with PostgreSQL schema definitions.
- Prevent accidental schema drift.
- Preserve modular boundaries.
- Make Flyway migrations and JPA validation consistent.

## 2. Entity Naming

Persistence entity classes should use the suffix:


Entity

Examples:

UserEntity
ProductEntity
ProductVariantEntity
OrderEntity
OrderItemEntity

The entity class name is a Java implementation detail.

Database table names must use plural lowercase snake case.

Example:

@Entity
@Table(name = "product_variants")
public class ProductVariantEntity {
}

Do not rely on the entity class name to infer business table names.

3. Entity Annotation

Every persistence entity must declare:

@Entity
@Table(name = "...")

Example:

@Entity
@Table(name = "products")
public class ProductEntity extends AuditableEntity {
}

The @Table annotation is required for business entities to ensure stable
database mappings.

4. Entity Inheritance

Entities requiring identifiers and optimistic locking should extend:

BaseEntity

Entities requiring identifiers, optimistic locking, creation timestamps, and
update timestamps should extend:

AuditableEntity

Preferred:

public class ProductEntity extends AuditableEntity {
}

Do not redeclare inherited fields such as:

id
version
createdAt
updatedAt

inside business entities.

5. Constructor Conventions

JPA entities must provide a protected no-argument constructor.

Example:

protected ProductEntity() {
}

A public no-argument constructor should be avoided unless required by an
external framework.

Entities should provide intention-revealing constructors or factory methods
for valid creation.

Example:

public ProductEntity(
        String name,
        String slug,
        BigDecimal price
) {
    this.name = name;
    this.slug = slug;
    this.price = price;
}

Constructors must not create invalid entity state.

6. Field Access

The project uses field-based JPA access.

Persistence annotations must be placed on fields rather than getters.

Preferred:

@Column(name = "product_code", nullable = false)
private String productCode;

Avoid mixing field and property access inside the same entity.

7. Column Mapping

Columns should be mapped explicitly when database semantics matter.

Example:

@Column(
        name = "product_code",
        nullable = false,
        length = 50
)
private String productCode;

Explicit names are required for:

semantic names different from Java fields;
boolean columns;
foreign-key columns;
monetary columns;
timestamps;
legacy mappings;
fields requiring stable schema names.

Simple fields whose Java and database names are identical may omit name.

Example:

@Column(nullable = false, length = 150)
private String name;
8. Nullability

Java and database nullability must agree.

Required value:

@Column(nullable = false)
private String name;

Optional value:

@Column
private String description;

Do not use primitive types for nullable columns.

Incorrect:

@Column
private int discountPercentage;

Preferred:

@Column
private Integer discountPercentage;

Database NOT NULL constraints must also be declared in Flyway migrations.

JPA annotations do not replace database constraints.

9. String Length

Every bounded business string should define an explicit maximum length.

Examples:

@Column(nullable = false, length = 150)
private String name;
@Column(nullable = false, length = 255)
private String email;
@Column(length = 1000)
private String description;

Do not rely on the default VARCHAR(255) without considering the business
meaning.

Large text should use:

@Lob
@Column(columnDefinition = "TEXT")

only when a demonstrated requirement exists.

For PostgreSQL text fields, prefer explicit Flyway type definitions and avoid
unnecessary columnDefinition in entities unless validation requires it.

10. UUID Mapping

Entity identifiers use Java:

UUID

and PostgreSQL:

UUID

Business entities must not redefine identifier generation.

The identifier is inherited from BaseEntity.

Foreign keys referencing UUID entities must also use UUID columns.

11. Boolean Mapping

Boolean Java fields should use semantic names.

Example:

@Column(name = "is_active", nullable = false)
private boolean active;
@Column(name = "is_verified", nullable = false)
private boolean verified;

Database boolean names should use is_ or has_ when it improves clarity.

Avoid nullable Boolean unless three states are intentionally required.

12. Timestamp Mapping

Timestamp fields use:

Instant

for UTC-based system timestamps.

Examples:

@Column(name = "expires_at")
private Instant expiresAt;
@Column(name = "paid_at")
private Instant paidAt;

PostgreSQL columns should use:

TIMESTAMP WITH TIME ZONE

or:

TIMESTAMPTZ

The project must not use LocalDateTime for globally meaningful timestamps.

Use LocalDate for date-only business values.

Example:

@Column(name = "birth_date")
private LocalDate birthDate;
13. Monetary Mapping

Monetary values must use:

BigDecimal

Example:

@Column(
        name = "unit_price",
        nullable = false,
        precision = 19,
        scale = 2
)
private BigDecimal unitPrice;

Do not use:

float
double

for monetary values.

The corresponding PostgreSQL type should be:

NUMERIC(19, 2)

The selected precision and scale must match the Flyway migration.

14. Quantity Mapping

Whole-number quantities should normally use:

Integer

or:

int

depending on nullability.

Example:

@Column(nullable = false)
private int quantity;

Large counters may use:

long

Quantities requiring decimal precision should use BigDecimal.

Business validity such as positive quantity must be enforced by:

domain/application validation;
database check constraints when appropriate.
15. Enum Mapping

Enums must be stored as text.

Required mapping:

@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 30)
private ProductStatus status;

Do not use:

@Enumerated(EnumType.ORDINAL)

Ordinal storage is prohibited because enum ordering can change.

Enum database values must remain stable after migrations are released.

Renaming a persisted enum constant requires migration analysis.

16. Default Values

Java field initializers may define object creation defaults.

Example:

@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 30)
private ProductStatus status = ProductStatus.DRAFT;

However, database defaults must also be defined explicitly in Flyway when
required.

Do not assume a Java default protects direct database inserts or other data
writers.

17. Column Definitions

Avoid using:

@Column(columnDefinition = "...")

for ordinary mappings.

It couples Java entities directly to database-specific DDL and can duplicate
Flyway responsibility.

Use it only when:

Hibernate validation cannot infer the required type;
a PostgreSQL-specific type is intentionally used;
the decision is documented and tested.

Flyway remains the source of truth for schema creation.

18. Generated Values

Database-generated values other than entity identifiers must be explicitly
designed.

Examples may include:

invoice number;
order number;
sequential public reference;
computed search column.

Do not use database-generated fields without defining:

creation ownership;
retrieval behavior;
uniqueness;
Flyway migration;
integration tests.
19. Immutable and Mutable Fields

Entities should expose behavior-oriented methods instead of unrestricted
setters.

Preferred:

public void rename(String name) {
    this.name = requireValidName(name);
}

Avoid:

public void setName(String name) {
    this.name = name;
}

for fields whose changes require validation or business rules.

Simple persistence-only test entities may use simpler methods.

20. Lombok Conventions

Do not use:

@Data

on JPA entities.

@Data generates:

setters for all fields;
equals;
hashCode;
toString;

which may cause problems with lazy relations and mutable persistence state.

Allowed when justified:

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

Avoid automatic toString generation for entities containing relationships.

21. Equality and Hash Code

Entity equality must not include all mutable fields.

Do not generate equals and hashCode from every field.

Until an explicit entity equality strategy is approved, entities should rely
on object identity or a carefully designed identifier-based implementation.

Relationships and mutable fields must not participate in generated equality.

22. Validation Annotations

Jakarta Bean Validation may be used on application request models.

Validation annotations on entities may be added only when they represent
persistence-independent invariants.

Do not rely exclusively on entity validation for API input validation.

Database constraints and application validation remain required where
appropriate.

23. Sensitive Data

Sensitive values must never be stored in plain text when secure storage is
required.

Examples:

password_hash
refresh_token_hash
verification_token_hash

Avoid entity fields such as:

password
rawToken
plainSecret

Sensitive fields must not appear in generated toString output.

24. Mapping Example
@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_products_slug",
                        columnNames = "slug"
                )
        },
        indexes = {
                @Index(
                        name = "idx_products_status",
                        columnList = "status"
                )
        }
)
public class ProductEntity extends AuditableEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(
            name = "unit_price",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected ProductEntity() {
    }
}
25. Prohibited Patterns

The following patterns are prohibited:

@Entity
public class ProductEntity {
}

Business entity without explicit @Table.

@Enumerated(EnumType.ORDINAL)
private ProductStatus status;

Ordinal enum persistence.

private double price;

Floating-point monetary value.

@Data
@Entity
public class UserEntity {
}

Unrestricted Lombok-generated entity behavior.

@Column(columnDefinition = "VARCHAR(255)")
private String name;

Unnecessary database-specific DDL duplication.

private LocalDateTime createdAt;

Local timestamp for globally meaningful audit data.

26. Review Checklist

Every entity must satisfy:

Declares @Entity.
Declares explicit @Table.
Extends the correct shared base entity.
Uses a protected no-argument constructor.
Uses field-based access consistently.
Declares appropriate nullability.
Declares meaningful string lengths.
Uses Instant for UTC timestamps.
Uses BigDecimal for money.
Uses EnumType.STRING.
Does not use Lombok @Data.
Does not expose unrestricted setters.
Does not duplicate schema creation logic from Flyway.
Matches the corresponding Flyway migration.
