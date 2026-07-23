package com.shoestore.shared.domain.exception;

/**
 * Marker contract implemented by module-owned domain error-code enums.
 *
 * <p>Domain error codes provide stable machine-readable identities for business-rule violations.
 * They must remain independent of transport, persistence, logging, localization, and framework
 * concerns.
 */
public interface DomainErrorCode {}
