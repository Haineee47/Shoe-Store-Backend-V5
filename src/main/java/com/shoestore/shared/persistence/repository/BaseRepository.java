package com.shoestore.shared.persistence.repository;

import com.shoestore.shared.persistence.BaseEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository abstraction for JPA entities in the application.
 *
 * <p>Business-specific repositories should extend this interface instead of extending Spring Data
 * repository interfaces directly.
 *
 * @param <T> entity type extending {@link BaseEntity}
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity>
    extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {}
