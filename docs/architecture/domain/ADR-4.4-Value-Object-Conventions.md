# ADR-4.4 — Value Object Conventions

Status: Accepted

Phase:
4.4.1 — Define Value Object Principles and Scope

---

# Context

Domain Model cần một chuẩn thống nhất cho Value Object để:

- tránh Primitive Obsession
- đóng gói business meaning
- đảm bảo bất biến (immutability)
- tập trung validation
- hỗ trợ Domain-Driven Design

---

# Decision

## Definition

Một Value Object:

- không có identity
- được xác định hoàn toàn bởi value
- immutable
- không có lifecycle độc lập
- luôn hợp lệ ngay sau khi được tạo

---

# Equality

Entity:

- equality dựa trên identity

Value Object:

- equality dựa trên toàn bộ value

Ví dụ:

Money(100, VND)
=
Money(100, VND)

---

# Identity

Value Object:

- không kế thừa BaseEntity
- không implement AggregateRoot
- không có UUID
- không có Version

---

# Construction

Value Object phải được tạo thông qua controlled construction.

Cho phép:

- constructor
- static factory

Không được tạo invalid instance.

---

# Validation

Validation thuộc trách nhiệm của chính Value Object.

Không để validation nằm rải rác trong Service.

---

# Normalization

Nếu domain yêu cầu, Value Object phải normalize dữ liệu đầu vào.

Ví dụ:

- trim
- uppercase SKU
- lowercase email

Normalization phải deterministic.

---

# Immutability

Value Object phải immutable.

Không được có:

- setter
- update(...)
- change(...)

Nếu cần thay đổi:

tạo Value Object mới.

---

# Defensive Copy

Nếu chứa:

- List
- Set
- Map
- array
- mutable object

phải defensive copy.

---

# Java Convention

Đối với scalar Value Object:

Java record là lựa chọn mặc định.

Ví dụ:

ProductName

Quantity

Money

Đối với Value Object phức tạp:

final class được phép.

---

# Shared Placement

Không đưa mọi Value Object vào shared.

Chỉ đưa vào shared khi:

- cùng business meaning
- cùng validation
- cùng lifecycle semantics

Ngược lại đặt trong module tương ứng.

---

# Persistence

Cho phép:

@Embeddable

hoặc

@Convert

Không được phụ thuộc:

- EntityManager
- JpaRepository
- Hibernate Session

---

# Exception

Hiện tại sử dụng:

- IllegalArgumentException
- NullPointerException

Domain Exception sẽ được chuẩn hóa ở Phase 4.6.

---

# Marker Interface

Không tạo:

ValueObject

marker interface.

Architecture sẽ dựa trên package và convention.

---

# Accepted Baseline

✓ Immutable

✓ Value Equality

✓ No Identity

✓ No Public Setter

✓ Validation During Construction

✓ Defensive Copy

✓ Record Preferred

✓ No BaseEntity

✓ No AggregateRoot

✓ No Repository

✓ No Lifecycle

---

# Collection and Mutable Components

Value Objects containing mutable components must perform defensive copying.

For collections:

- use `List.copyOf`
- use `Set.copyOf`
- use `Map.copyOf`

Read-only wrappers over mutable backing collections are not sufficient.

Java records provide shallow immutability only. Mutable record components must
be copied explicitly during construction.

Collection type must reflect domain semantics:

- `List` when order is part of the value
- `Set` when uniqueness matters and order does not
- `Map` when key-value association is part of the value

Arrays require defensive copies on both input and output.

---

# Architecture Rules

Production Value Objects must satisfy:

- must not extend BaseEntity
- must not implement AggregateRoot
- must not be annotated with @Entity
- must not depend on Spring Framework
- must not depend on Hibernate
- must not depend on repositories
- must not depend on EntityManager
- must expose immutable state only

Architecture rules are continuously enforced through ArchUnit tests.

---

# Consequences

Positive consequences:

- domain values cannot exist in invalid states
- equality semantics are predictable
- mutable inputs cannot change existing values
- Value Objects remain independent from infrastructure
- business meaning is not scattered across services

Trade-offs:

- additional domain types may increase the number of classes
- construction requires explicit validation and normalization
- mutable components require manual defensive copying
- JPA mapping may require embeddables or converters
