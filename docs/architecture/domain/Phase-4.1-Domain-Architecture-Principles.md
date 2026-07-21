# Phase 4.1 — Domain Architecture Principles

## Trạng thái

- **Phase:** 4
- **Sub-phase:** 4.1
- **Status:** IN PROGRESS
- **Verification:** NOT STARTED
- **Frozen:** NO

## Mục tiêu

Định nghĩa kiến trúc Domain chính thức của dự án.

Sau Phase này, mọi module nghiệp vụ phải tuân theo cùng một bộ nguyên tắc.

Phase này không viết code.

Chúng ta chỉ xác lập baseline kiến trúc.

## 4.1.1 Domain Responsibility

Domain là nơi chứa:

- Business Rules
- Business State
- Business Behavior
- Business Invariants

Ví dụ:

Một Product phải biết:

- đổi giá như thế nào
- khi nào được deactivate
- khi nào được activate
- khi nào không được xóa

không phải Application Service.

### Domain được phép chứa

- ✔ Entity
- ✔ Aggregate Root
- ✔ Value Object
- ✔ Domain Event
- ✔ Domain Repository Interface
- ✔ Domain Exception
- ✔ Domain Error Code
- ✔ Domain Policy
- ✔ Specification (nếu cần)

### Domain KHÔNG được chứa

- ❌ REST API
- ❌ Controller
- ❌ DTO
- ❌ Response Model
- ❌ Request Model
- ❌ Repository Implementation
- ❌ SQL
- ❌ EntityManager
- ❌ JdbcTemplate
- ❌ Spring MVC
- ❌ Security Filter
- ❌ Email Sender
- ❌ Payment SDK
- ❌ Redis
- ❌ Kafka
- ❌ RabbitMQ

## 4.1.2 Layer Responsibility

Chúng ta sử dụng Layered Architecture.

```text
Presentation
        │
        ▼
Application
        │
        ▼
Domain
        ▲
        │
Infrastructure
```

### Presentation

Chỉ xử lý:

- HTTP
- Validation của Request DTO
- Authentication
- Authorization
- Response

Không chứa business rule.

Ví dụ:

```text
POST /products

Controller
    ↓
Request DTO
    ↓
Application Service
    ↓
Response DTO
```

### Application

Application chịu trách nhiệm:

- Use Case
- Transaction
- gọi Repository
- gọi Domain
- Publish Domain Event
- gọi External Service

Application không chứa business rule phức tạp.

Ví dụ:

```text
createProduct()
    ↓
Product.create(...)
    ↓
repository.save(...)
```

### Domain

Domain là trái tim hệ thống.

Chỉ Domain được quyết định:

- Có được activate không?
- Có được đổi giá không?
- Có được hủy không?
- Có được xóa không?

### Infrastructure

Infrastructure triển khai:

- JPA
- PostgreSQL
- Flyway
- Redis
- SMTP
- JWT
- Storage
- Kafka

Infrastructure không quyết định business.

## 4.1.3 Dependency Rule

```text
Presentation
    ↓
Application
    ↓
Domain
    ↑
Infrastructure
```

Infrastructure implement interface của Domain.

Ví dụ:

```text
Domain
ProductRepository
    ↓
Infrastructure
JpaProductRepository
```

Không được:

```text
Domain
    ↓
Infrastructure
```

Ví dụ cấm:

```java
@Entity
public class Product {

    @Autowired
    JdbcTemplate jdbcTemplate;

}
```

## 4.1.4 Business Behavior Rule

Entity phải có behavior.

Ví dụ tốt:

```java
product.changePrice(newPrice);
product.activate();
product.deactivate();
product.reserveStock(quantity);
```

Không dùng:

```java
product.setPrice(...);
product.setActive(...);
product.setQuantity(...);
```

## 4.1.5 Anemic Domain Model

Không chấp nhận:

```java
public class Product {

    private String name;
    private BigDecimal price;

}
```

Mọi logic nằm trong Service.

Đây là **Anemic Domain Model**.

Chấp nhận:

```java
public class Product {

    public void changePrice(...) {
    }

    public void activate() {
    }

    public void deactivate() {
    }

}
```

## 4.1.6 Domain Invariant

Invariant luôn được bảo vệ trong Domain.

Ví dụ:

- Price >= 0
- SKU không rỗng
- Role phải tồn tại
- Order không thể Completed rồi Cancel
- Permission không được duplicate

Không phụ thuộc:

- `@NotNull`
- `@NotBlank`

để bảo vệ invariant.

Bean Validation chỉ dành cho Request.

## 4.1.7 Aggregate Boundary

Aggregate Root là điểm vào duy nhất.

```text
Order
├── OrderLine
├── Shipment
└── PaymentReference
```

Không sửa `OrderLine` từ bên ngoài.

Chỉ:

```java
order.changeLineQuantity(...);
```

## 4.1.8 Entity Construction

Không cho phép:

```java
Product p = new Product();

p.setName(...);
p.setPrice(...);
```

Entity phải luôn hợp lệ sau khi tạo.

Ví dụ:

```java
Product.create(...)
```

hoặc

```java
new Product(...)
```

## 4.1.9 State Transition

Mỗi transition phải có method riêng.

- activate()
- deactivate()
- confirm()
- cancel()
- ship()
- complete()
- changePrice()
- rename()

Không dùng:

```java
setStatus(...)
```

## 4.1.10 Domain Purity

Domain không được biết:

- Spring
- Hibernate Session
- REST
- JSON
- Database
- Kafka
- Redis
- JWT

Domain chỉ biết:

- Business

# Kiến trúc chính thức sau Phase 4.1

```text
Presentation
        │
        ▼
Application
        │
        ▼
Domain
        ▲
        │
Infrastructure
```

Trong đó:

- **Presentation:** HTTP, DTO, authentication, response.
- **Application:** điều phối use case, transaction, repository, event.
- **Domain:** business behavior, aggregate, value object, invariant, domain event, repository contract.
- **Infrastructure:** JPA, PostgreSQL, Flyway, Redis, SMTP, JWT và các tích hợp kỹ thuật.

# Acceptance Criteria

Phase 4.1 hoàn thành khi thống nhất được:

- [ ] Trách nhiệm của từng layer.
- [ ] Quy tắc phụ thuộc giữa các layer.
- [ ] Vai trò của Domain.
- [ ] Quy tắc Aggregate Root.
- [ ] Quy tắc Entity.
- [ ] Quy tắc Business Behavior.
- [ ] Quy tắc Domain Invariant.
- [ ] Quy tắc State Transition.
- [ ] Quy tắc Domain Purity.

## Sau khi bạn phê duyệt Phase 4.1

Chúng ta sẽ chuyển sang **Phase 4.2 — Domain Package and Module Structure**, nơi sẽ thiết kế cấu trúc package chuẩn cho toàn bộ các module của dự án trước khi bắt đầu triển khai User, Role, Permission và các module nghiệp vụ khác.
