package com.shoestore.shared.persistence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Enables automatic population of persistence audit timestamps. */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {}
