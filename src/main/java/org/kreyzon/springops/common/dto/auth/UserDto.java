package org.kreyzon.springops.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.kreyzon.springops.auth.model.User;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for {@link org.kreyzon.springops.auth.model.User}.
 * Represents a lightweight, immutable version of the User entity for data exchange.
 * Includes user details such as ID, username, email, and timestamps.
 * This class is marked as {@link Value} to make it immutable and thread-safe.
 * Implements {@link Serializable} for potential use in distributed systems.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Jacksonized
public class UserDto implements Serializable {

    /**
     * Unique identifier for the user.
     */
    UUID id;

    /**
     * Username of the user.
     */
    @NotNull
    @Size(min = 3, max = 50)
    String username;

    /**
     * Email address of the user.
     */
    @NotNull
    @Email
    String email;

    /**
     * Encrypted password of the user.
     */
    @Size(min = 8, max = 100)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
             message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character.")
    String password;

    /**
     * Timestamp indicating when the user was created.
     */
    Instant createdAt;

    /**
     * Timestamp indicating the last time the user was updated.
     */
    Instant updatedAt;

    /**
     * Converts a {@link User} entity to a {@link UserDto}.
     *
     * @param user the {@link User} entity to convert.
     * @return a {@link UserDto} representing the given entity.
     */
    public static UserDto fromEntity(User user) {
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            null, // Avoid exposing the password in DTOs
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    /**
     * Converts this {@link UserDto} to a {@link User} entity.
     *
     * @return a {@link User} entity representing this DTO.
     */
    public static User toEntity(UserDto userDto) {
        return new User(
            userDto.getId(),
            userDto.getUsername(),
            userDto.getEmail(),
            userDto.getPassword(),
            userDto.getCreatedAt(),
            userDto.getUpdatedAt()
        );
    }
}