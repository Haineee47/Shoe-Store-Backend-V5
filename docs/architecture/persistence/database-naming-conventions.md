# Database Naming Conventions

## 1. Purpose

This document defines database naming conventions for the Shoe Store Backend.

The goals are:

- Keep database objects consistent and predictable.
- Make Flyway migrations easy to review.
- Keep JPA mappings aligned with PostgreSQL.
- Prevent naming differences between modules.
- Improve maintainability and schema discoverability.

## 2. General Naming Style

All PostgreSQL identifiers must use:


lowercase_snake_case

Correct:

users
product_variants
created_at
role_permissions
fk_user_roles_user

Incorrect:

Users
productVariants
ProductVariant
createdAt
ROLE_PERMISSIONS

Identifiers must not rely on quoted PostgreSQL names.

Avoid:

CREATE TABLE "User" (...);

Use:

CREATE TABLE users (...);
3. Table Naming

Table names must:

Use lowercase snake case.
Use plural nouns.
Describe the stored business records.
Avoid technical prefixes unless required by infrastructure.
Avoid PostgreSQL reserved keywords.

Examples:

users
roles
permissions
products
product_variants
product_images
shopping_carts
shopping_cart_items
orders
order_items
payments

Avoid:

user
order
productEntity
tbl_products
t_product
Reserved keywords

Names such as the following should not be used directly:

user
order
group
constraint
authorization

Use safer alternatives:

users
orders
user_groups
4. Join Table Naming

Many-to-many join tables must combine the owning table names.

Format:

<left_table>_<right_table>

Examples:

user_roles
role_permissions
product_categories

The order should remain consistent across:

table name;
foreign-key columns;
JPA mapping;
Flyway migration.
5. Column Naming

Column names must use lowercase snake case.

Examples:

id
created_at
updated_at
version
email
password_hash
product_id
unit_price
total_amount
is_active

Java fields may use camel case:

private Instant createdAt;
private BigDecimal unitPrice;
private boolean active;

The corresponding database columns must be:

created_at
unit_price
is_active
6. Primary Key Naming

Every business entity table must use:

id

as its primary-key column.

The standard identifier type is:

UUID

Example:

id UUID PRIMARY KEY

Do not use table-prefixed primary keys such as:

user_id
product_id
order_id

inside the entity's own table.

Foreign-key columns must use the referenced entity name:

user_id
product_id
order_id
role_id
7. Foreign-Key Column Naming

Foreign-key columns must follow:

<referenced_entity>_id

Examples:

user_id
role_id
product_id
category_id
order_id

For more specific relationships, use the business role:

created_by_user_id
approved_by_user_id
shipping_address_id
billing_address_id
parent_category_id

Avoid ambiguous names:

reference_id
entity_id
owner
related_id

unless the model is intentionally polymorphic and documented.

8. Boolean Column Naming

Boolean columns must use a clear state or condition.

Preferred:

is_active
is_deleted
is_verified
has_discount
requires_shipping

Avoid:

active_flag
deleted_flag
status_boolean
value

Java field names may omit the is prefix where JavaBean conventions require it:

private boolean active;

Database column:

is_active
9. Timestamp Column Naming

Timestamp columns must use the _at suffix.

Examples:

created_at
updated_at
deleted_at
verified_at
paid_at
cancelled_at
expires_at

Date-only columns must use the _date suffix when needed:

birth_date
delivery_date

Avoid ambiguous names:

created
updated
time
date
timestamp
10. Monetary Column Naming

Monetary columns must describe the represented value.

Examples:

unit_price
subtotal_amount
discount_amount
shipping_amount
tax_amount
total_amount
refund_amount

A currency column should be named:

currency_code

Use ISO currency codes when multi-currency support is introduced.

Avoid generic names:

price_value
money
amount_value
total

when the exact meaning is unclear.

11. Quantity and Measurement Naming

Quantity columns should use:

quantity
available_quantity
reserved_quantity
sold_quantity

Measurement columns must include their unit when the unit is fixed.

Examples:

weight_grams
length_centimeters
height_centimeters

If the unit is variable, store a separate unit column.

12. Status and Type Columns

Enum-backed columns should use semantic names:

status
payment_status
order_status
product_type
discount_type

Do not append technical suffixes such as:

status_enum
type_code_value

Enum values stored as text must use stable uppercase names unless a different
business representation is explicitly required.

Examples:

ACTIVE
INACTIVE
PENDING
COMPLETED
CANCELLED
13. Version Column

Entities using optimistic locking must use:

version

Example:

version BIGINT NOT NULL DEFAULT 0

Do not use:

row_version
entity_version
lock_version

unless required by an external integration.

14. Soft-Delete Columns

Soft deletion must not be assumed for every table.

When explicitly required, use:

is_deleted
deleted_at
deleted_by_user_id

Do not add soft-delete columns to all tables preemptively.

15. Constraint Naming

Constraints must have explicit names.

Primary key:

pk_<table>

Examples:

pk_users
pk_products
pk_order_items

Foreign key:

fk_<table>_<referenced_table>

Examples:

fk_user_roles_users
fk_user_roles_roles
fk_order_items_orders
fk_order_items_products

When more than one foreign key references the same table, include the role:

fk_orders_created_by_users
fk_orders_approved_by_users

Unique constraint:

uk_<table>_<column_or_business_key>

Examples:

uk_users_email
uk_products_slug
uk_product_variants_sku

Check constraint:

ck_<table>_<rule>

Examples:

ck_products_price_non_negative
ck_order_items_quantity_positive
16. Index Naming

Indexes must use:

idx_<table>_<columns>

Examples:

idx_users_email
idx_products_status
idx_products_category_id
idx_orders_user_id_created_at

Unique indexes may use:

uidx_<table>_<columns>

when implemented as a PostgreSQL unique index instead of a table constraint.

Examples:

uidx_users_normalized_email
17. Sequence Naming

The project uses UUID identifiers and does not use database sequences by
default.

If a sequence is explicitly required, use:

seq_<table>_<column>

Example:

seq_invoices_number
18. Schema Naming

The default PostgreSQL schema is:

public

Business tables must not declare different schemas without an approved
architecture decision.

Flyway history remains managed by Flyway using its standard schema history
table.

19. JPA Mapping Rule

Entity mappings must explicitly document database-sensitive names where
clarity or schema stability matters.

Examples:

@Entity
@Table(name = "product_variants")
public class ProductVariantEntity {
}
@Column(name = "created_at", nullable = false)
private Instant createdAt;

A Hibernate physical naming strategy may provide camelCase-to-snake_case
conversion, but explicit mappings remain required for:

table names;
join tables;
foreign-key columns;
unique constraints;
indexes;
columns whose semantic database name differs from the Java field name.
20. Prohibited Naming Patterns

The following patterns are prohibited:

tbl_users
t_users
userTable
UserEntity
column1
value1
data
info
ref_id
status_flag
createdDateTime

Database names must describe their business or persistence meaning clearly.

21. Review Checklist

Every new table or mapping must satisfy:

Uses lowercase snake case.
Uses a plural table name.
Avoids PostgreSQL reserved words.
Uses id as the primary-key column.
Uses UUID identifiers unless explicitly approved otherwise.
Uses explicit and meaningful foreign-key column names.
Uses _at for timestamps.
Uses clear boolean names.
Uses explicit constraint names.
Uses explicit index names.
Does not introduce unnecessary soft-delete columns.
Matches the corresponding JPA entity mapping.

---

## 2. Baseline quyết định chính

### Table dùng số nhiều

Đúng:


users
products
orders
order_items

Không dùng:

user
product
order
orderItem
Database dùng snake case

Java:

private Instant createdAt;
private BigDecimal unitPrice;

PostgreSQL:

created_at
unit_price
Primary key luôn là id

Trong bảng users:

id

Không phải:

user_id

Nhưng trong bảng khác tham chiếu đến users:

user_id
Constraint phải có tên rõ ràng

Không nên phụ thuộc vào tên PostgreSQL tự sinh:

users_email_key

Nên khai báo rõ:

uk_users_email



## Hibernate Naming Strategy

The application explicitly configures the following Hibernate naming
strategies:

Implicit strategy:
org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy

Physical strategy:
org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy


The physical naming strategy converts logical camel-case names to
lowercase snake-case names.

Examples:

createdAt         -> created_at
productVariant    -> product_variant
shippingAddressId -> shipping_address_id

The naming strategy is a safety baseline and does not replace explicit
mapping for schema-sensitive objects.

Explicit names remain required for:

business table names;
join tables;
foreign-key columns;
boolean columns using is_ or has_;
unique constraints;
check constraints;
indexes;
semantic names that differ from Java field names.

Entity class suffixes are not removed automatically. Therefore an entity
such as ProductEntity must explicitly declare:

@Table(name = "products")

to avoid an inferred table name such as product_entity.



