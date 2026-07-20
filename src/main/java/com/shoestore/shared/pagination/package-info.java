/**
 * Provides shared pagination and sorting models used by application use cases.
 *
 * <p>
 * The package isolates application and API-facing pagination contracts from
 * Spring Data implementation details while still allowing controlled
 * conversion to {@code Pageable}.
 * </p>
 *
 * <p>
 * Page indexes are zero-based. Potentially unbounded queries must use
 * pagination, and requested page sizes must respect the configured maximum.
 * </p>
 */
package com.shoestore.shared.pagination;
