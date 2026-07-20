# Repository Conventions

## 1. Purpose

This document defines the repository conventions for the Shoe Store Backend.

The goals are:

- Keep persistence access consistent across modules.
- Preserve modular boundaries.
- Prevent business logic from leaking into repositories.
- Ensure list queries support pagination.
- Provide a consistent approach for dynamic filtering.

## 2. Base Repository

All Spring Data JPA repositories must extend:

BaseRepository<T>

Business repositories must not extend JpaRepository or
JpaSpecificationExecutor directly.

Example:

public interface ProductJpaRepository
        extends BaseRepository<ProductEntity> {
}
3. Repository Ownership

Business-specific repositories must remain inside their owning module.

Correct:

modules/product/infrastructure/persistence/repository
modules/user/infrastructure/persistence/repository
modules/order/infrastructure/persistence/repository

Incorrect:

shared/persistence/repository/ProductRepository
shared/persistence/repository/UserRepository

The shared repository package may contain only reusable persistence
abstractions that are independent of business modules.

4. Repository Responsibilities

Repositories are responsible only for persistence operations, including:

Saving entities.
Retrieving entities.
Checking entity existence.
Deleting entities when deletion is permitted.
Executing persistence queries.
Supporting pagination, sorting, and filtering.

Repositories must not contain:

Business workflows.
Authorization decisions.
HTTP concerns.
Response DTO construction.
Application logging orchestration.
Cross-module coordination.
Transactional use-case logic.
5. Query Method Conventions

Derived query methods may be used for simple and explicit queries.

Examples:

Optional<UserEntity> findByEmailIgnoreCase(String email);

boolean existsByEmailIgnoreCase(String email);

Optional<ProductEntity> findBySlug(String slug);

Method names must remain readable.

Avoid long derived query methods such as:

findByStatusAndCategoryIdAndPriceGreaterThanEqualAndPriceLessThanEqualAndNameContainingIgnoreCaseOrderByCreatedAtDesc(...)

Complex or optional filtering must use Specification.

6. Specification Conventions

Use Specification<T> when:

Filters are optional.
Multiple filters can be combined.
Search criteria are dynamic.
The query would otherwise require a long derived method name.

Specifications belonging to a business module must remain inside that module.

Example location:

modules/product/infrastructure/persistence/specification

Specifications must describe persistence predicates only and must not
implement business decisions.

7. Pagination Conventions

Queries returning potentially unbounded collections must use pagination.

Preferred:

Page<ProductEntity> findAll(
        Specification<ProductEntity> specification,
        Pageable pageable
);

Avoid using:

repository.findAll();

inside application or business services when the number of records can grow.

Unpaged queries are acceptable only when the result set is inherently bounded,
such as:

A small fixed reference list.
A query constrained by a unique value.
A relationship known to have a strict business limit.

Any exception must be intentional and reviewable.

8. Sorting Conventions

Sorting must be provided through Pageable or Sort.

Preferred:

PageRequest.of(page, size, Sort.by("createdAt").descending());

Avoid creating repository methods solely to express sorting when Spring Data
sorting already supports the requirement.

9. Optional Conventions

Queries that may return no record must return:

Optional<T>

Do not return null for a missing entity.

Example:

Optional<UserEntity> findByEmailIgnoreCase(String email);

Conversion from an empty Optional to an application or domain exception
belongs outside the repository.

10. Collection Conventions

Repository methods returning multiple entities must return collection
interfaces rather than concrete implementations.

Preferred:

List<ProductEntity>
Page<ProductEntity>
Slice<ProductEntity>

Avoid:

ArrayList<ProductEntity>

Repository methods must never return null collections.

11. Entity and DTO Boundaries

JPA repositories operate on persistence entities.

Repositories must not return:

API response models.
Controller DTOs.
ApiResponse.
ErrorResponse.

Projection interfaces or query-specific persistence projections may be used
only when a demonstrated performance or query requirement exists.

12. Transaction Boundaries

Repository interfaces do not define use-case transaction boundaries.

Transactions should normally be controlled by the application service that
coordinates the use case.

Preferred:

@Transactional
public void updateProduct(...) {
    // coordinate repository operations
}

Use @Transactional(readOnly = true) for application-level read operations
when appropriate.

Do not annotate every repository method manually when Spring Data already
provides suitable transaction behavior.

13. Flush Conventions

Use:

save(entity);

for normal persistence operations.

Use:

saveAndFlush(entity);
flush();

only when the current use case must force SQL execution before the surrounding
transaction completes, such as:

Verifying a database constraint during a test.
Requiring a generated database value immediately.
Ensuring SQL is executed before a subsequent dependent operation.

Do not use saveAndFlush by default.

14. Delete Conventions

Hard deletion must be used only when allowed by the business and data retention
rules.

Do not introduce generic methods such as:

softDelete(...)
restore(...)
findAllActive(...)

into BaseRepository.

Soft-delete behavior, when required, must be designed explicitly for the
relevant entities and modules.

15. Native Query and JPQL Conventions

Use the following preference order:

Spring Data built-in repository methods.
Readable derived query methods.
Specification.
JPQL.
Native SQL.

JPQL or native SQL may be used when simpler mechanisms cannot express the
required query correctly or efficiently.

Native queries require:

A clear justification.
Integration-test coverage.
Review of PostgreSQL-specific behavior.
Explicit mapping of returned data.
16. Fetching Conventions

Do not solve lazy-loading problems by changing all relationships to eager
loading.

Use an explicit query approach when related data is required:

EntityGraph
JPQL fetch join
Query projection
Dedicated read query

Avoid unbounded collection fetch joins together with pagination because they
can produce incorrect or inefficient results.

17. Repository Naming

Spring Data implementation repositories should use the suffix:

JpaRepository

Examples:

ProductJpaRepository
UserJpaRepository
OrderJpaRepository

Domain repository ports, if introduced, should use business-oriented names:

ProductRepository
UserRepository
OrderRepository

This distinction prevents confusion between domain contracts and Spring Data
infrastructure interfaces.

18. Prohibited Patterns

The following patterns are prohibited:

repository.findAll();

for potentially unbounded business data.

repository.findById(id).orElseThrow(...);

with HTTP-specific exceptions inside the repository.

@Query(...)
ApiResponse<ProductResponse> findProduct(...);

Returning controller or response models from repositories.

default void processOrder(...) {
    // business workflow
}

Implementing business behavior inside repository interfaces.

19. Review Checklist

Every new repository must satisfy the following:

Extends BaseRepository.
Belongs to its owning module.
Contains no business workflow.
Uses Optional for nullable single results.
Uses pagination for potentially unbounded results.
Uses readable derived methods only for simple queries.
Uses Specification for dynamic filtering.
Does not expose API response models.
Does not introduce unnecessary JPQL or native SQL.
Has integration-test coverage for custom queries.

## 2. Cập nhật `package-info.java`

Mở:

src/main/java/com/shoestore/shared/persistence/repository/package-info.java

Cập nhật thành:

/**
 * Provides shared repository abstractions for the persistence layer.
 *
 * <p>
 * This package contains reusable repository contracts that are independent
 * of individual business modules.
 * </p>
 *
 * <p>
 * Business-specific repositories must remain inside their owning modules.
 * Repository implementations are responsible only for data access and must
 * not contain business workflows, HTTP concerns, authorization decisions, or
 * API response construction.
 * </p>
 *
 * <p>
 * Potentially unbounded queries must use pagination. Simple queries may use
 * Spring Data derived query methods, while dynamic filtering should use
 * {@code Specification}.
 * </p>
 */
package com.shoestore.shared.persistence.repository;
Các quyết định chính
Repository nghiệp vụ phải kế thừa BaseRepository

Đúng:

public interface ProductJpaRepository
        extends BaseRepository<ProductEntity> {
}

Không dùng:

public interface ProductJpaRepository
        extends JpaRepository<ProductEntity, UUID> {
}
Không gọi findAll() cho dữ liệu có thể tăng không giới hạn

Không nên:

List<ProductEntity> products = productRepository.findAll();

Nên dùng:

Pageable pageable = PageRequest.of(page, size);

Page<ProductEntity> products =
        productRepository.findAll(pageable);
Query đơn giản dùng derived query
Optional<UserEntity> findByEmailIgnoreCase(String email);

boolean existsByEmailIgnoreCase(String email);
Filter động dùng Specification
Specification<ProductEntity> specification =
        Specification.where(hasStatus(status))
                .and(hasCategory(categoryId))
                .and(priceBetween(minPrice, maxPrice));

Page<ProductEntity> result =
        productRepository.findAll(specification, pageable);
Repository không ném exception thuộc HTTP

Không làm:

default ProductEntity getRequired(UUID id) {
    return findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND
            ));
}

Việc chuyển kết quả không tồn tại thành ApplicationException phải được thực hiện tại application service.
