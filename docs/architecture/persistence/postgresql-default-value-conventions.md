# PostgreSQL Default Value Conventions

## Status

Official and frozen persistence convention after Phase 3 acceptance.

## Core Rule

Every default value has exactly one owner:

- Application-owned; or
- Database-owned.

Application ownership is the default choice.

## Application-Owned Defaults

Use for:

- Business state
- Boolean flags
- Enum initial state
- Numeric counters
- Values required before persistence

Application-owned columns must not define a PostgreSQL DEFAULT.

## Database-Owned Defaults

Use only for:

- Database-generated infrastructure values
- Values that must also be generated for non-JPA inserts
- Approved database timestamp generation

Database-owned columns must be defined in Flyway and mapped so Hibernate
does not insert or update them.

## Forbidden

- Dual ownership
- Hibernate schema generation
- @ColumnDefault as schema source of truth
- @DynamicInsert as a general default strategy
- columnDefinition used only to declare DEFAULT
- Database triggers for ordinary defaults
- Defaults that hide missing mandatory input
- Editing an applied migration

## SQL DEFAULT Semantics

A PostgreSQL default is applied only when the column is omitted from INSERT
or the DEFAULT keyword is used.

An explicit NULL does not invoke the default.

## Migration Rule

All database default additions, removals, and changes require a new Flyway
migration.
