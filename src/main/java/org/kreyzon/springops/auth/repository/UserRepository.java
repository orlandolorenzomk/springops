package org.kreyzon.springops.auth.repository;

import org.kreyzon.springops.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link User} entities.
 * Extends Spring Data JPA's {@link JpaRepository} to provide CRUD operations and query methods.
 * <p>
 * This interface is a Spring-managed component, annotated with {@link Repository}.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}