# Relationship and Foreign-Key Mapping Conventions

## 1. Purpose

This document defines JPA relationship and foreign-key mapping conventions
for the Shoe Store Backend.

The goals are:

- Keep entity relationships explicit and predictable.
- Preserve aggregate and module boundaries.
- Prevent accidental eager loading.
- Avoid unsafe cascade operations.
- Keep JPA mappings aligned with Flyway migrations.
- Prevent circular serialization and N+1 query problems.
- Make ownership and lifecycle responsibilities clear.

## 2. General Principles

Relationships must represent real domain associations.

A relationship must not be added only for navigation convenience.

Before introducing a JPA relationship, determine:

- Which entity owns the relationship?
- Which entity owns the lifecycle?
- Is the relationship required or optional?
- Is bidirectional navigation genuinely necessary?
- Can the association cross a module boundary?
- What happens when either side is deleted?
- How will the relationship be fetched?
- How will the database foreign key be indexed?

The default preference is:


unidirectional relationship

Bidirectional relationships must be introduced only when both navigation
directions are required by demonstrated use cases.

3. Relationship Ownership

The entity containing the foreign-key column owns the relationship.

Example:

order_items.order_id -> orders.id

Therefore:

OrderItemEntity

owns the JPA relationship to:

OrderEntity

Example:

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(
        name = "order_id",
        nullable = false,
        foreignKey = @ForeignKey(
                name = "fk_order_items_orders"
        )
)
private OrderEntity order;

The inverse collection, when required, uses:

@OneToMany(mappedBy = "order")
private List<OrderItemEntity> items;
4. Foreign-Key Column Naming

Foreign-key columns must use:

<referenced_entity>_id

Examples:

user_id
product_id
order_id
category_id
role_id

When the relationship role is more specific, include the role:

created_by_user_id
approved_by_user_id
billing_address_id
shipping_address_id
parent_category_id

Avoid ambiguous names:

reference_id
owner_id
related_id
target_id
entity_id

unless the model is intentionally polymorphic and documented.

5. Foreign-Key Constraint Naming

Every foreign-key constraint must have an explicit name.

Format:

fk_<owning_table>_<referenced_table_or_role>

Examples:

fk_order_items_orders
fk_order_items_products
fk_user_roles_users
fk_user_roles_roles
fk_orders_shipping_addresses
fk_orders_created_by_users

The same constraint name must appear consistently in:

Flyway migration;
JPA @ForeignKey;
architecture documentation where relevant.
6. Many-to-One Relationships

@ManyToOne is the preferred representation for ordinary foreign-key
relationships.

Every @ManyToOne relationship must explicitly declare:

fetch = FetchType.LAZY

Example:

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(
        name = "category_id",
        nullable = false,
        foreignKey = @ForeignKey(
                name = "fk_products_categories"
        )
)
private CategoryEntity category;

Do not rely on JPA's default eager loading for @ManyToOne.

Required relationship:

optional = false
nullable = false

Optional relationship:

optional = true
nullable = true

JPA optionality and database nullability must agree.

7. One-to-Many Relationships

A @OneToMany collection should normally be the inverse side of a
@ManyToOne relationship.

Preferred:

@OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true
)
private final List<OrderItemEntity> items = new ArrayList<>();

Use cascade and orphan removal only when the parent truly owns the child
lifecycle.

Examples of possible parent-owned children:

Order -> OrderItem
ShoppingCart -> ShoppingCartItem
Product -> ProductImage

Examples that normally do not share lifecycle:

User -> Role
Product -> Category
Order -> User

Do not create unidirectional @OneToMany using an automatically generated
join table unless that join table is intentionally designed.

8. One-to-One Relationships

Use @OneToOne only when the relationship is genuinely one-to-one and the
database enforces uniqueness.

Example:

@OneToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(
                name = "fk_customer_profiles_users"
        )
)
private UserEntity user;

The corresponding database must include a unique constraint:

uk_customer_profiles_user_id

Do not use @OneToOne merely because only one related record currently
exists.

If the relationship may become one-to-many later, model it according to the
business invariant rather than current sample data.

9. Many-to-Many Relationships

Direct @ManyToMany mappings should be avoided for business relationships.

A join entity is preferred when the association:

has timestamps;
has status;
has ordering;
has metadata;
may require auditing;
may acquire attributes later;
needs explicit lifecycle behavior.

Preferred:

UserEntity
UserRoleEntity
RoleEntity

instead of:

@ManyToMany
private Set<RoleEntity> roles;

A direct @ManyToMany mapping may be used only when:

the join table contains exactly two foreign keys;
no additional association attributes exist;
lifecycle behavior is simple;
the decision is documented.
10. Join Entity Conventions

A join entity must be modeled as a normal entity.

Example:

@Entity
@Table(
        name = "user_roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_roles_user_id_role_id",
                        columnNames = {
                                "user_id",
                                "role_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_roles_role_id",
                        columnList = "role_id"
                )
        }
)
public class UserRoleEntity extends AuditableEntity {
}

The join entity should normally contain:

id
user_id
role_id
version
created_at
updated_at

A composite primary key must not be introduced without an explicit
architecture decision.

UUID surrogate identifiers remain the project baseline.

11. Fetch Strategy

The default relationship fetch strategy is:

LAZY

Explicitly declare lazy loading for:

@ManyToOne
@OneToOne
@OneToMany
@ManyToMany

Examples:

@ManyToOne(fetch = FetchType.LAZY)
@OneToMany(fetch = FetchType.LAZY)

Collections are lazy by default, but the declaration may be included where it
improves clarity.

Do not change a relationship to EAGER to fix a lazy initialization problem.

Fetch requirements must be solved through:

transaction boundaries;
JPQL fetch joins;
entity graphs;
DTO projections;
targeted repository queries.
12. Cascade Conventions

Cascade operations must reflect lifecycle ownership.

Do not use:

cascade = CascadeType.ALL

automatically on every relationship.

Common acceptable parent-child mapping:

@OneToMany(
        mappedBy = "order",
        cascade = {
                CascadeType.PERSIST,
                CascadeType.MERGE
        },
        orphanRemoval = true
)

CascadeType.REMOVE must be treated carefully.

It may be acceptable when:

the child has no independent lifecycle;
no other entity references the child;
deleting the parent must delete all children.

Do not cascade remove from:

Order -> User
Product -> Category
User -> Role

Never cascade from a child to a shared parent.

Incorrect:

@ManyToOne(
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL
)
private UserEntity user;
13. Orphan Removal

Use:

orphanRemoval = true

only when removing a child from the parent's collection means that the child
must be deleted from the database.

Appropriate example:

OrderItem removed from Order

Potentially inappropriate example:

Role removed from User

because the role still exists independently.

Orphan removal is a lifecycle decision, not a collection cleanup setting.

14. Collection Types

Use:

List

when:

ordering matters;
duplicates are prevented through business rules or database constraints;
the aggregate naturally exposes ordered children.

Use:

Set

when:

membership uniqueness matters;
equality behavior is safely defined;
ordering is not required.

Because entity equality can be difficult before persistence, List is often
safer for entity child collections.

Collections must be initialized immediately.

Preferred:

private final List<OrderItemEntity> items =
        new ArrayList<>();

Avoid:

private List<OrderItemEntity> items;

Do not replace managed collections after persistence.

Incorrect:

this.items = newItems;

Preferred:

this.items.clear();
this.items.addAll(newItems);

when replacement semantics are genuinely required.

15. Bidirectional Relationship Synchronization

Bidirectional relationships must provide helper methods that maintain both
sides.

Example:

public void addItem(OrderItemEntity item) {
    if (item == null) {
        throw new IllegalArgumentException(
                "Order item must not be null"
        );
    }

    items.add(item);
    item.attachTo(this);
}
public void removeItem(OrderItemEntity item) {
    if (items.remove(item)) {
        item.detachFrom(this);
    }
}

The relationship should not expose unrestricted collection setters.

Avoid:

public void setItems(List<OrderItemEntity> items) {
    this.items = items;
}
16. Read-Only Collection Exposure

Do not expose a mutable internal collection directly.

Avoid:

public List<OrderItemEntity> getItems() {
    return items;
}

Preferred:

public List<OrderItemEntity> getItems() {
    return Collections.unmodifiableList(items);
}

or:

public List<OrderItemEntity> getItems() {
    return List.copyOf(items);
}

When Hibernate proxy behavior or performance makes copying undesirable, use
an unmodifiable view and document the choice.

17. Cross-Module Relationships

A JPA entity relationship must not automatically cross module boundaries.

Before mapping a direct entity association across modules, determine whether
the dependency violates modular ownership.

Possible approaches include:

storing the referenced UUID only;
querying through an application port;
using an integration event;
using a documented read model;
introducing an approved module dependency.

For loosely coupled module references, prefer:

@Column(name = "customer_id", nullable = false)
private UUID customerId;

over:

@ManyToOne
private CustomerEntity customer;

when direct entity navigation would create an undesirable module dependency.

Cross-module entity relationships require explicit architectural review.

18. Identifier-Only References

An identifier-only reference may be used when:

the related entity belongs to another module;
no persistence navigation is required;
only identity is needed;
aggregate boundaries should remain independent.

Example:

@Column(
        name = "customer_id",
        nullable = false,
        updatable = false
)
private UUID customerId;

The database may still define a foreign key if both modules share the same
database and coupling is accepted.

Whether to create the database foreign key is an architectural decision,
separate from whether JPA maps an entity relationship.

19. Foreign-Key Indexes

PostgreSQL does not automatically create indexes for foreign-key columns.

Foreign-key columns used for joins, lookups, deletion checks, or filtering
should normally be indexed.

Example:

order_items.order_id

should use:

idx_order_items_order_id

A composite index may replace an individual foreign-key index when its leading
column satisfies the same access pattern.

Indexes must be introduced based on expected query patterns, not blindly for
every column.

20. Database Delete Behavior

Foreign-key delete behavior must be explicitly chosen in Flyway.

Possible options:

ON DELETE RESTRICT
ON DELETE CASCADE
ON DELETE SET NULL

Default preference:

ON DELETE RESTRICT

Use ON DELETE CASCADE only for strict lifecycle-owned children.

Example candidate:

order_items.order_id -> orders.id

Use ON DELETE SET NULL only when:

the relation is optional;
preserving the child is required;
null has a valid business meaning.

JPA cascade and database cascade must not contradict each other.

21. Updating Foreign-Key Relationships

Relationship changes must use intention-revealing methods.

Preferred:

public void changeCategory(CategoryEntity category) {
    this.category = Objects.requireNonNull(
            category,
            "Category must not be null"
    );
}

Avoid unrestricted setters when a relationship change has domain meaning.

A relationship marked immutable should use:

@JoinColumn(
        name = "order_id",
        nullable = false,
        updatable = false
)

when the child cannot move to another parent after creation.

22. N+1 Query Prevention

Lazy loading is the default, but uncontrolled traversal may cause N+1
queries.

Do not solve N+1 by changing mappings to eager loading.

Preferred solutions:

JPQL fetch join
@EntityGraph
DTO projection
batch fetching
dedicated query

Fetch only the associations required by a specific use case.

Repository methods that fetch associations must make the behavior clear.

Example:

Optional<OrderEntity> findWithItemsById(UUID id);

Implementation may use an entity graph or explicit JPQL.

23. Pagination and Collection Fetching

Do not combine pageable queries with collection fetch joins without verifying
Hibernate and SQL behavior.

A fetch join over a to-many relationship may:

duplicate root rows;
distort pagination;
perform pagination in memory;
produce incorrect totals.

Preferred strategies:

page root identifiers first, then fetch details;
use DTO projections;
use batch fetching;
issue a second targeted query for collections.
24. Serialization Boundary

JPA entities must not be returned directly from controllers.

This prevents:

lazy initialization errors;
circular serialization;
accidental data exposure;
unstable API contracts;
excessive queries during serialization.

Do not use Jackson annotations as the primary fix for persistence relationship
cycles.

Avoid relying on:

@JsonIgnore
@JsonManagedReference
@JsonBackReference

inside persistence entities as an API design strategy.

Map entities to application or API response models.

25. Lombok and Relationships

Do not include relationships in generated:

equals
hashCode
toString

Do not use:

@Data

on relationship-bearing entities.

A generated toString may trigger lazy loading or infinite recursion.

A generated equals or hashCode may break collection behavior when
identifiers change after persistence.

26. Relationship Mapping Example
@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(
                        name = "idx_orders_customer_id",
                        columnList = "customer_id"
                )
        }
)
public class OrderEntity extends AuditableEntity {

    @Column(
            name = "customer_id",
            nullable = false,
            updatable = false
    )
    private UUID customerId;

    @OneToMany(
            mappedBy = "order",
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            },
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private final List<OrderItemEntity> items =
            new ArrayList<>();

    protected OrderEntity() {
    }

    public List<OrderItemEntity> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(OrderItemEntity item) {
        Objects.requireNonNull(
                item,
                "Order item must not be null"
        );

        items.add(item);
        item.attachTo(this);
    }

    public void removeItem(OrderItemEntity item) {
        if (items.remove(item)) {
            item.detachFrom(this);
        }
    }
}
@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(
                        name = "idx_order_items_order_id",
                        columnList = "order_id"
                ),
                @Index(
                        name = "idx_order_items_product_id",
                        columnList = "product_id"
                )
        }
)
public class OrderItemEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_order_items_orders"
            )
    )
    private OrderEntity order;

    @Column(
            name = "product_id",
            nullable = false,
            updatable = false
    )
    private UUID productId;

    protected OrderItemEntity() {
    }

    void attachTo(OrderEntity order) {
        this.order = Objects.requireNonNull(order);
    }

    void detachFrom(OrderEntity order) {
        if (this.order == order) {
            this.order = null;
        }
    }
}
27. Prohibited Patterns

The following patterns are prohibited unless explicitly approved.

Implicit eager many-to-one:

@ManyToOne
private UserEntity user;

Unsafe cascade:

@ManyToOne(
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL
)
private CategoryEntity category;

Uncontrolled many-to-many:

@ManyToMany
private Set<RoleEntity> roles;

Uninitialized collection:

@OneToMany(mappedBy = "order")
private List<OrderItemEntity> items;

Mutable collection setter:

public void setItems(List<OrderItemEntity> items) {
    this.items = items;
}

Direct entity serialization:

@GetMapping
public OrderEntity getOrder() {
}

Ambiguous foreign key:

@JoinColumn(name = "reference_id")

Relationship without explicit foreign-key name:

@JoinColumn(name = "order_id")
28. Review Checklist

Every relationship must satisfy:

Has a demonstrated domain purpose.
Defines the owning side clearly.
Uses explicit FetchType.LAZY.
Uses an explicit join-column name.
Uses an explicit foreign-key constraint name.
Aligns optionality with database nullability.
Uses cascade only when lifecycle ownership requires it.
Uses orphan removal only for true lifecycle-owned children.
Initializes collection fields.
Does not expose mutable collection setters.
Synchronizes both sides when bidirectional.
Does not create an unnecessary cross-module entity dependency.
Has an appropriate foreign-key index where required.
Defines database delete behavior intentionally.
Does not expose entities directly through the API.
Has a query strategy that avoids uncontrolled N+1 behavior.


# 2. Baseline quyết định

## Quan hệ mặc định là unidirectional

Ví dụ sản phẩm thuộc category:


@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(
        name = "category_id",
        nullable = false,
        foreignKey = @ForeignKey(
                name = "fk_products_categories"
        )
)
private CategoryEntity category;

Không nhất thiết phải thêm ngay:

@OneToMany(mappedBy = "category")
private List<ProductEntity> products;

vào CategoryEntity.

Chỉ thêm chiều ngược lại khi có use case thực tế cần truy cập trực tiếp từ category sang collection product.

@ManyToOne luôn khai báo LAZY

JPA mặc định:

@ManyToOne → EAGER
@OneToOne  → EAGER

Đây không phải baseline của dự án.

Luôn khai báo:

@ManyToOne(fetch = FetchType.LAZY)

và:

@OneToOne(fetch = FetchType.LAZY)

Không dựa vào mặc định của JPA.

Không dùng CascadeType.ALL một cách tự động

Ví dụ sai:

@ManyToOne(
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL
)
private CategoryEntity category;

Nếu xóa product, cascade có thể tác động đến category dùng chung.

Quan hệ từ child đến shared parent thường không có cascade:

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(...)
private CategoryEntity category;
3. Sửa lỗi nhỏ trong ví dụ synchronization

Trong tài liệu ở trên, phương thức removeItem nên truyền parent hiện tại cho child:

public void removeItem(OrderItemEntity item) {
    if (items.remove(item)) {
        item.detachFrom(this);
    }
}

Child:

void detachFrom(OrderEntity order) {
    if (this.order == order) {
        this.order = null;
    }
}

Không dùng:

item.detachFrom();

nếu method thực tế yêu cầu xác nhận parent.
