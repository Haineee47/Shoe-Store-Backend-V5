# ADR-4.5 — Domain Service Conventions

Status: Accepted

Phase:
4.5.1 — Define Domain Service Principles and Scope

## Context

Some domain operations require collaboration between multiple domain objects and
do not naturally belong to a single Entity, Aggregate Root or Value Object.

Without explicit conventions, Domain Services can become transaction scripts,
repository wrappers, Spring services or generic containers for business logic.

## Decision

A Domain Service represents domain behavior that:

- has clear business meaning;
- does not naturally belong to one domain object;
- coordinates multiple domain values or objects;
- remains independent from application orchestration and infrastructure.

## Ownership

Domain Services belong to the domain package of the module that owns the
business capability.

Shared placement is allowed only when semantics and ownership are genuinely
shared.

## Statelessness

Domain Services are stateless by default.

Immutable domain configuration is allowed.

Mutable runtime state is prohibited.

## Dependencies

Domain Services must not depend on:

- Spring Framework;
- repositories;
- EntityManager;
- JdbcTemplate;
- Hibernate Session;
- HTTP clients;
- messaging clients;
- caches;
- file-system APIs;
- presentation DTOs.

## Inputs and Outputs

Inputs must be domain types.

Outputs should be Value Objects, domain decisions, enums or immutable domain
result objects.

## Determinism

Domain Services should be deterministic whenever possible.

Time, randomness and other changing inputs must be supplied explicitly.

## Naming

Class and method names must use domain language.

Generic names such as DomainService, BusinessLogicService, HelperService,
Manager or Processor are prohibited unless they are actual domain terms.

## Interfaces

Interfaces are introduced only when a real business variation point or module
boundary requires one.

A single implementation does not justify an interface by itself.

## Transactions and Side Effects

Domain Services do not:

- manage transactions;
- load or save aggregates;
- publish events directly;
- call external systems;
- mutate infrastructure state.

Application Services orchestrate those responsibilities.

## Exceptions

Simple Java exceptions remain permitted until Domain Exception conventions are
established in Phase 4.6.

## Consequences

Positive:

- business rules remain explicit;
- domain logic is independently testable;
- infrastructure concerns stay outside the domain;
- aggregate and Value Object responsibilities remain focused.

Trade-offs:

- application services must load required domain objects before invoking a
  Domain Service;
- developers must decide carefully whether behavior belongs to an existing
  domain object;
- unnecessary Domain Services are rejected.

## Stateless Fixture

The conventions are demonstrated through a test-only allocation policy.

The fixture accepts an explicitly requested quantity and available quantity,
then returns an immutable allocation decision.

The policy:

- has no mutable fields;
- does not depend on Spring;
- does not access repositories;
- does not perform infrastructure I/O;
- receives domain values;
- returns an immutable domain result;
- produces deterministic results;
- validates operation-level preconditions.

A dedicated non-negative quantity fixture is used because allocation outcomes
may legitimately contain zero values.

The existing positive quantity fixture is not reused where its invariant would
misrepresent the allocation domain.


## Cross-Domain-Object Business Logic

The allocation policy demonstrates a business decision requiring both a
requested quantity and an available quantity.

Neither input independently contains enough information to determine:

- the allocated quantity;
- the remaining available quantity;
- the unfulfilled quantity;
- the allocation status.

The following conservation rules must always hold:

- requested quantity equals allocated quantity plus unfulfilled quantity;
- available quantity equals allocated quantity plus remaining available
  quantity;
- allocated quantity never exceeds requested quantity;
- allocated quantity never exceeds available quantity.

Tests verify observable domain behavior rather than private implementation
details.

Mocks are unnecessary because the Domain Service is stateless and has no
infrastructure dependencies.


## Input, Output and Invariant Boundaries

Domain Service inputs use validated domain types rather than primitives,
presentation DTOs or unstructured containers.

Intrinsic value validity is protected by the input Value Objects.

The Domain Service validates operation-specific preconditions that cannot be
expressed as intrinsic Value Object invariants.

The immutable domain result protects its own structural consistency.

For the allocation fixture:

- allocation quantities must be non-negative;
- requested quantity must be greater than zero for an allocation operation;
- available quantity may be zero;
- a fully allocated decision must have a positive allocated quantity and zero
  unfulfilled quantity;
- a partially allocated decision must have positive allocated and unfulfilled
  quantities;
- a not-allocated decision must have zero allocated quantity and a positive
  unfulfilled quantity.

Conservation rules involving the original request and availability are verified
at the Domain Service behavior boundary rather than duplicated inside the
result object.


## Statelessness and Determinism

Domain Services must not retain mutable runtime state between invocations.

Equivalent input values must produce equivalent output values when the
business rule is deterministic.

The result of one invocation must not depend on:

- a previous invocation;
- invocation order;
- service instance identity;
- system time;
- random number generation;
- environment variables;
- JVM system properties;
- default locale or timezone;
- static mutable state;
- thread-local or request-scoped business context.

Time, randomness, locale, timezone and other changing business inputs must be
supplied explicitly when they are relevant to the decision.

Immutable domain configuration may be held in final fields. The prohibition
applies to mutable runtime state, not to all fields.

The test fixture declares no instance fields because it requires no
configuration. This fixture-specific restriction must not be generalized into
a rule that prohibits immutable configuration in production Domain Services.


## Dependency Boundaries

Domain Services may depend on:

- Java language and immutable utility types;
- Aggregate Roots;
- Entities;
- Value Objects;
- domain enums;
- immutable domain results;
- pure domain policy abstractions with demonstrated variation points;
- domain exceptions and error codes after their convention is established.

Domain Services must not depend on:

- Spring Framework;
- repositories, including domain repository interfaces;
- JPA or Hibernate runtime APIs;
- JDBC or SQL APIs;
- web or servlet APIs;
- HTTP clients;
- messaging clients or publishers;
- cache or Redis APIs;
- security context APIs;
- logging abstractions;
- runtime configuration APIs;
- application services, use cases, commands, queries or DTOs;
- presentation or infrastructure packages.

The application layer loads required domain objects and external values before
invoking a Domain Service.

The application layer also owns:

- transaction boundaries;
- persistence;
- external communication;
- event publishing;
- logging;
- mapping between transport types and domain types.

Time, locale, timezone, actor information and similar changing values must be
provided explicitly as domain inputs when they influence a business decision.

A Domain Service may hold immutable domain configuration, but it must not read
configuration directly from Spring, environment variables or JVM system
properties.


## Architecture Enforcement

Production Domain Services are identified by their placement in:

- `com.shoestore.modules.<module>.domain.service`;
- `com.shoestore.shared.domain.service`.

Test fixtures are excluded from production architecture scanning.

ArchUnit rules enforce that production Domain Services:

- use domain-oriented service names;
- do not depend on Spring Framework;
- do not depend on repositories;
- do not depend on JPA, Hibernate, JDBC or SQL APIs;
- do not depend on application, presentation or infrastructure layers;
- do not depend on web, messaging, caching or Redis APIs;
- do not depend on security context APIs;
- do not depend on logging frameworks or application logging abstractions;
- are not annotated as Spring components;
- do not declare transaction boundaries;
- do not declare mutable static business state.

The architecture rules allow an empty production Domain Service package because
no production Domain Service should be created before a real business rule
requires one.

Supporting domain values and decision results should not be placed in
`domain.service`. They belong in the appropriate domain model, value, type or
decision package.

The rules do not prohibit immutable instance configuration. A Domain Service
may hold constructor-provided immutable domain policy values in final fields.

Method-level calls such as `LocalDate.now()`, `Instant.now()` and
`UUID.randomUUID()` are not prohibited by broad package rules because their
types may also be valid explicit domain inputs. Hidden time and randomness are
therefore additionally controlled through convention, tests and review.


## Freeze Record

Phase 4.5 — Domain Service Conventions was frozen after:

- targeted Domain Service verification completed successfully;
- production architecture rules completed successfully;
- full project regression completed successfully;
- PostgreSQL integration tests completed successfully;
- no existing architecture or persistence baseline regressed.

The conventions identified as DS-021 through DS-100 are part of the accepted
domain architecture baseline.

Any future change that weakens these conventions requires:

1. an explicit change request;
2. documented architectural rationale;
3. impact analysis;
4. updated tests;
5. an accepted ADR amendment or replacement.
