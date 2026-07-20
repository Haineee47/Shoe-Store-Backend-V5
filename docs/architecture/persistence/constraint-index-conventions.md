# Constraint and Index Conventions

## 1. Purpose

This document defines constraint and index conventions for the Shoe Store
Backend PostgreSQL database.

The goals are:

- Protect data integrity at the database level.
- Keep constraint and index names predictable.
- Align Flyway migrations with JPA mappings.
- Prevent unnecessary or duplicate indexes.
- Support expected query patterns efficiently.
- Make migration review and production diagnosis easier.

## 2. Source of Truth

Flyway migrations are the source of truth for:

- table definitions;
- primary keys;
- foreign keys;
- unique constraints;
- check constraints;
- indexes;
- partial indexes;
- functional indexes;
- delete and update behavior.

JPA annotations document entity mappings and support Hibernate schema
validation.

JPA annotations must not be treated as the production schema-generation
mechanism.

The application must continue using:


spring.jpa.hibernate.ddl-auto=validate

or an equivalent non-mutating schema policy.

Do not use:

create
create-drop
update

for shared or production environments.

3. General Naming Style

All constraint and index names must use:

lowercase_snake_case

Names must be explicit and stable.

Do not rely on automatically generated PostgreSQL or Hibernate names.

Correct:

pk_products
fk_order_items_orders
uk_products_slug
ck_order_items_quantity_positive
idx_orders_customer_id_created_at

Incorrect:

products_pkey
fk8dj32j2j4
UK_6DOTKOTT2KJSP8VW4D0M25FB7
index_1
4. PostgreSQL Identifier Length

PostgreSQL identifiers are limited to 63 bytes.

Constraint and index names must remain concise enough to avoid automatic
truncation.

Avoid names such as:

fk_relationship_child_test_entities_relationship_parent_test_entities

Prefer a clear but shorter role-based name:

fk_relationship_children_parent

A shortened name must remain understandable and unique within the schema.

Do not depend on PostgreSQL truncation because it can cause:

confusing schema names;
name collisions;
migration failures;
difficult production diagnostics.
5. Primary-Key Constraints

Every table must have an explicit primary-key constraint.

Format:

pk_<table>

Examples:

pk_users
pk_products
pk_orders
pk_order_items

Example migration:

CREATE TABLE products (
    id UUID NOT NULL,

    CONSTRAINT pk_products
        PRIMARY KEY (id)
);

The project baseline uses:

UUID

with the column:

id

Do not use database-generated sequential identifiers unless explicitly
approved.

6. Foreign-Key Constraints

Every foreign key must have an explicit constraint name.

Format:

fk_<owning_table>_<referenced_table_or_role>

Examples:

fk_products_categories
fk_order_items_orders
fk_order_items_products
fk_orders_customer
fk_orders_created_by

When multiple columns reference the same table, include the relationship role.

Example:

fk_orders_created_by
fk_orders_approved_by

Migration example:

CONSTRAINT fk_order_items_orders
    FOREIGN KEY (order_id)
    REFERENCES orders (id)
    ON DELETE RESTRICT

Foreign-key behavior must be explicitly designed.

Preferred default:

ON DELETE RESTRICT

Use:

ON DELETE CASCADE

only for strict lifecycle-owned children.

Use:

ON DELETE SET NULL

only when:

the relationship is optional;
preserving the referencing row is required;
null represents valid business state.
7. Unique Constraints

A unique constraint protects a business invariant.

Format:

uk_<table>_<column_or_business_key>

Examples:

uk_users_email
uk_products_slug
uk_product_variants_sku
uk_user_roles_user_id_role_id

Migration example:

CONSTRAINT uk_products_slug
    UNIQUE (slug)

JPA example:

@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_products_slug",
                        columnNames = "slug"
                )
        }
)

Unique constraints must not be added merely as a performance optimization.

Their primary purpose is data integrity.

8. Composite Unique Constraints

Use a composite unique constraint when uniqueness depends on multiple columns.

Examples:

user_id + role_id
product_id + size_id + color_id
order_id + line_number

Migration example:

CONSTRAINT uk_user_roles_user_id_role_id
    UNIQUE (user_id, role_id)

Column ordering must remain consistent in:

Flyway migration;
JPA @UniqueConstraint;
documentation;
related composite indexes.

The order should reflect the most common access pattern where practical.

9. Case-Insensitive Uniqueness

PostgreSQL unique constraints are case-sensitive for ordinary text columns.

A constraint such as:

UNIQUE (email)

allows both:

User@example.com
user@example.com

when their exact text differs.

Case-insensitive business keys require an explicit strategy.

Approved strategies may include:

storing a normalized column;
using a functional unique index;
using PostgreSQL citext after an approved decision.

Example using a functional unique index:

CREATE UNIQUE INDEX uidx_users_lower_email
    ON users (LOWER(email));

The application should normalize the value consistently before persistence.

Do not assume application validation alone is enough to guarantee uniqueness.

10. Unique Constraint Versus Unique Index

Use a unique constraint when the rule can be expressed directly as uniqueness
over table columns.

Example:

CONSTRAINT uk_products_slug
    UNIQUE (slug)

Use a unique index when PostgreSQL-specific behavior is required.

Examples:

functional uniqueness;
partial uniqueness;
expression-based uniqueness.

Example:

CREATE UNIQUE INDEX uidx_users_lower_email
    ON users (LOWER(email));

Do not create both a unique constraint and an equivalent unique index because
PostgreSQL already creates an underlying unique index for a unique constraint.

11. Check Constraints

Check constraints protect row-level data invariants.

Format:

ck_<table>_<rule>

Examples:

ck_products_unit_price_non_negative
ck_order_items_quantity_positive
ck_product_variants_stock_non_negative
ck_orders_total_amount_non_negative
ck_promotions_date_range_valid

Migration example:

CONSTRAINT ck_order_items_quantity_positive
    CHECK (quantity > 0)

Another example:

CONSTRAINT ck_promotions_date_range_valid
    CHECK (
        end_at IS NULL
        OR end_at > start_at
    )

Check constraint names should describe the rule, not repeat the SQL expression.

Avoid:

ck_orders_total_amount_greater_than_or_equal_zero

Prefer:

ck_orders_total_amount_non_negative
12. Appropriate Check Constraints

Suitable database check constraints include:

positive quantities;
non-negative monetary values;
valid percentage ranges;
valid date ordering;
bounded rating values;
mutually dependent nullable fields;
simple state-independent conditions.

Examples:

CHECK (quantity > 0)
CHECK (unit_price >= 0)
CHECK (
    discount_percentage >= 0
    AND discount_percentage <= 100
)
CHECK (
    end_at IS NULL
    OR end_at > start_at
)

Do not put complex workflows or cross-table business logic into check
constraints.

13. Application Validation and Database Constraints

Database constraints and application validation serve different purposes.

Application validation provides:

user-friendly error messages;
early rejection;
use-case-specific validation.

Database constraints provide:

final data-integrity protection;
protection from concurrent writes;
protection from alternative database writers;
protection against programming defects.

Critical invariants should be enforced in both appropriate layers.

Example:

Order item quantity must be positive.

Application:

if (quantity <= 0) {
    throw new IllegalArgumentException(...);
}

Database:

CONSTRAINT ck_order_items_quantity_positive
    CHECK (quantity > 0)
14. Index Naming

Ordinary indexes use:

idx_<table>_<columns>

Examples:

idx_products_status
idx_products_category_id
idx_orders_customer_id
idx_orders_customer_id_created_at
idx_order_items_order_id

Unique PostgreSQL indexes use:

uidx_<table>_<expression_or_columns>

Examples:

uidx_users_lower_email
uidx_products_active_slug

Names should identify the indexed columns or business expression.

15. Index Selection Principles

Create an index only when supported by:

a known query pattern;
a relationship lookup;
sorting or filtering requirements;
uniqueness enforcement;
measured performance evidence;
a credible expected workload.

Indexes are not free.

Every index increases:

storage usage;
insert cost;
update cost;
delete cost;
vacuum and maintenance work.

Do not index every column automatically.

16. Primary-Key and Unique Index Duplication

PostgreSQL automatically creates indexes for:

primary-key constraints;
unique constraints.

Do not create duplicate indexes for the same leading columns.

Incorrect:

CONSTRAINT pk_products PRIMARY KEY (id);

CREATE INDEX idx_products_id
    ON products (id);

The second index is redundant.

Also avoid:

CONSTRAINT uk_products_slug UNIQUE (slug);

CREATE INDEX idx_products_slug
    ON products (slug);

unless a materially different index definition is required.

17. Foreign-Key Indexes

PostgreSQL does not automatically create an index on the referencing side of a
foreign key.

Example:

order_items.order_id

should normally be indexed because it is likely used for:

loading order items;
joining orders and items;
validating parent deletion;
filtering by order.

Example:

CREATE INDEX idx_order_items_order_id
    ON order_items (order_id);

Not every foreign key must automatically receive a separate index.

A composite index may already cover it when the foreign key is the leading
column.

Example:

CREATE INDEX idx_orders_customer_id_created_at
    ON orders (customer_id, created_at DESC);

This may make a separate:

idx_orders_customer_id

unnecessary.

18. Composite Index Column Order

Composite index order must reflect query predicates and sorting.

Example query:

SELECT *
FROM orders
WHERE customer_id = ?
ORDER BY created_at DESC;

Suitable index:

CREATE INDEX idx_orders_customer_id_created_at
    ON orders (customer_id, created_at DESC);

The leading column rule matters.

An index on:

(customer_id, created_at)

can support queries by:

customer_id

but generally does not efficiently support queries filtering only by:

created_at

Column ordering must be designed from actual access patterns.

19. Indexing Low-Cardinality Columns

Avoid indexing low-cardinality columns in isolation unless evidence supports
it.

Examples:

is_active
is_deleted
status

An index solely on a boolean column often provides little benefit.

Instead, consider a partial or composite index aligned with a query.

Example:

CREATE INDEX idx_products_active_created_at
    ON products (created_at DESC)
    WHERE is_active = TRUE;

Do not introduce partial indexes without a demonstrated query requirement.

20. Partial Indexes

Partial indexes are PostgreSQL-specific indexes covering only rows matching a
predicate.

Example:

CREATE INDEX idx_orders_pending_created_at
    ON orders (created_at)
    WHERE status = 'PENDING';

Use partial indexes when:

queries repeatedly target a small subset;
the predicate is stable;
the subset is significantly smaller than the whole table.

Partial indexes must be created in Flyway.

They cannot be fully represented by standard JPA @Index.

Document their purpose in the migration.

21. Functional Indexes

Functional indexes index an expression rather than a raw column.

Example:

CREATE INDEX idx_users_lower_email
    ON users (LOWER(email));

Use functional indexes when queries consistently use the same expression.

The application query must match the indexed expression.

For case-insensitive uniqueness:

CREATE UNIQUE INDEX uidx_users_lower_email
    ON users (LOWER(email));

Functional indexes remain Flyway-only schema objects.

22. Covering Indexes

PostgreSQL supports included columns.

Example:

CREATE INDEX idx_products_category_id_created_at
    ON products (category_id, created_at DESC)
    INCLUDE (name, unit_price);

Use covering indexes only after query analysis demonstrates a benefit.

Do not add INCLUDE columns speculatively.

They increase index size and write cost.

23. Sort Direction

PostgreSQL B-tree indexes can often scan in either direction.

Explicit direction may still be useful in composite ordering patterns.

Example:

CREATE INDEX idx_orders_customer_created
    ON orders (customer_id, created_at DESC);

Do not duplicate otherwise identical ascending and descending indexes without
evidence.

24. Null Values and Uniqueness

A PostgreSQL unique constraint normally allows multiple null values.

For an optional column:

external_reference

this may be acceptable.

If only one null or special null semantics are needed, design the rule
explicitly.

Do not assume UNIQUE means only one row may contain null.

25. Nullable Columns in Composite Unique Constraints

Composite unique constraints involving nullable columns require careful
review because PostgreSQL may allow multiple rows where one component is null.

Example:

UNIQUE (product_id, external_variant_code)

may allow multiple rows with the same product_id and a null code.

When this is not acceptable, use:

NOT NULL;
a partial unique index;
NULLS NOT DISTINCT where supported and intentionally selected;
a normalized sentinel only when justified.
26. JPA Table-Level Constraint Mapping

Standard constraints and indexes should be reflected in @Table where JPA
supports them.

Example:

@Entity
@Table(
        name = "product_variants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_variants_sku",
                        columnNames = "sku"
                )
        },
        indexes = {
                @Index(
                        name = "idx_product_variants_product_id",
                        columnList = "product_id"
                ),
                @Index(
                        name =
                                "idx_product_variants_product_id_is_active",
                        columnList = "product_id, is_active"
                )
        }
)
public class ProductVariantEntity {
}

JPA mapping and Flyway must match exactly.

However, JPA annotations do not represent all PostgreSQL capabilities.

The following remain Flyway-only:

partial indexes;
functional indexes;
included columns;
operator-class-specific indexes;
advanced PostgreSQL index methods;
complex check constraints where annotations are insufficient.
27. Column-Level Unique Mapping

Avoid relying only on:

@Column(unique = true)

because it does not provide a controlled constraint name.

Preferred:

@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_products_slug",
                        columnNames = "slug"
                )
        }
)

This gives the constraint a stable explicit name.

@Column(unique = true) should generally not be used for business entities.

28. Index Methods

The default index method is:

B-tree

It supports common:

equality;
range;
ordering;
prefix composite queries.

Other PostgreSQL index methods require demonstrated need.

Examples:

GIN
GiST
BRIN
hash

Potential uses:

GIN for full-text search or JSONB;
GiST for geometric or range data;
BRIN for very large naturally ordered tables.

Introducing a non-B-tree index requires documentation and performance
justification.

29. Search Indexes

Do not implement product search by adding speculative indexes to every text
column.

Search design may later use:

normalized text columns;
PostgreSQL full-text search;
trigram indexes;
a dedicated search service.

Any use of extensions such as:

pg_trgm
citext

requires an approved technical decision and Flyway migration.

30. Index Lifecycle

Indexes must be reviewed when:

queries change;
columns are removed;
constraints are replaced;
duplicate indexes appear;
write load increases;
production query plans reveal inefficiency.

Removing an index requires the same migration discipline as creating one.

Do not remove an index merely because it appears unused in a short local test.

31. Concurrent Index Creation

PostgreSQL supports:

CREATE INDEX CONCURRENTLY

This is useful for production tables because it reduces blocking.

However, concurrent index creation cannot run inside an ordinary transaction
block.

Flyway migrations using concurrent index operations require special migration
configuration and must not be introduced casually.

For the current foundation phase and empty schemas, use ordinary:

CREATE INDEX

Production-scale online index strategy will be addressed when required.

32. Constraint Validation Strategy

Constraints should normally be created and validated in the same migration.

For large existing production tables, PostgreSQL supports staged approaches
such as:

NOT VALID
VALIDATE CONSTRAINT

This is not required during the current foundation stage.

Any staged constraint deployment requires a documented migration plan.

33. Constraint Failure Handling

Database constraint failures must eventually be translated into stable
application errors.

Examples:

uk_users_email
    -> USER_EMAIL_ALREADY_EXISTS

ck_order_items_quantity_positive
    -> INVALID_ORDER_ITEM_QUANTITY

Do not expose raw PostgreSQL constraint messages through the API.

Constraint-to-domain error translation belongs to the exception handling and
application layers, not the entity mapping layer.

34. Migration Review Checklist

Every new constraint or index migration must verify:

Name is explicit.
Name follows lowercase snake case.
Name is within PostgreSQL identifier limits.
Constraint enforces a real invariant.
JPA mapping matches where applicable.
Foreign-key delete behavior is explicit.
Foreign-key index requirement was reviewed.
No duplicate index already exists.
Composite column order matches query patterns.
Unique constraints are not duplicated by indexes.
Check constraints are understandable.
PostgreSQL-specific indexes are documented.
Roll-forward impact is understood.
The full migration set validates successfully.
35. Prohibited Patterns

Unnamed JPA uniqueness:

@Column(unique = true)
private String slug;

Duplicate primary-key index:

CREATE INDEX idx_products_id
    ON products (id);

Duplicate unique index:

CONSTRAINT uk_products_slug UNIQUE (slug);

CREATE INDEX idx_products_slug
    ON products (slug);

Indexing every boolean column:

CREATE INDEX idx_products_is_active
    ON products (is_active);

Unreviewed broad composite index:

CREATE INDEX idx_orders_all
    ON orders (
        customer_id,
        status,
        payment_status,
        created_at,
        updated_at
    );

Automatically generated constraint names:

FOREIGN KEY (order_id)
    REFERENCES orders (id);

Unbounded expression index without a query requirement:

CREATE INDEX idx_products_lower_name
    ON products (LOWER(name));
36. Review Checklist

Every table must satisfy:

Has an explicitly named primary key.
Has explicitly named foreign keys.
Has explicitly named unique constraints.
Has explicitly named check constraints where needed.
Does not duplicate primary-key or unique indexes.
Reviews indexes for foreign-key columns.
Uses composite indexes based on access patterns.
Avoids speculative low-cardinality indexes.
Keeps JPA table metadata aligned with Flyway.
Uses Flyway for PostgreSQL-specific index features.
