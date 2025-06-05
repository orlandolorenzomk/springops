package org.kreyzon.springops.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity class representing a user in the system.
 * Maps to the "user" table in the database.
 * Provides fields for user details such as username, email, and password.
 * Includes timestamps for creation and updates.
 * <p>
 * This class uses Lombok annotations for boilerplate code reduction.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Getter
@Setter
@Entity
@Table(name = "\"user\"")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Unique identifier for the user.
     * Maps to the "id" column in the database.
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * Username of the user.
     * Maps to the "username" column in the database.
     * Cannot be null.
     */
    @Column(name = "username", nullable = false)
    private String username;

    /**
     * Email address of the user.
     * Maps to the "email" column in the database.
     * Cannot be null.
     */
    @Column(name = "email", nullable = false)
    private String email;

    /**
     * Encrypted password of the user.
     * Maps to the "password" column in the database.
     * Cannot be null.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Timestamp indicating when the user was created.
     * Maps to the "created_at" column in the database.
     * Cannot be null.
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Timestamp indicating the last time the user was updated.
     * Maps to the "updated_at" column in the database.
     * Can be null.
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

}