package org.kreyzon.springops.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.auth.model.User;
import org.kreyzon.springops.auth.repository.UserRepository;
import org.kreyzon.springops.common.dto.auth.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing user-related operations.
 * Provides methods for creating, retrieving, updating, and deleting users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Finds a user by their unique ID.
     *
     * @param userId the unique ID of the user to find.
     * @return a {@link UserDto} representing the user.
     * @throws RuntimeException if the user is not found.
     */
    public UserDto findById(UUID userId) {
        log.info("Fetching user with ID: {}", userId);
        return userRepository.findById(userId)
                .map(UserDto::fromEntity)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Retrieves all users.
     *
     * @return a list of {@link UserDto} objects representing all users.
     */
    public List<UserDto> findAll() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        log.info("Total users found: {}", users.size());
        return users.stream()
                .map(UserDto::fromEntity)
                .toList();
    }

    /**
     * Creates a new user.
     *
     * @param userDto the {@link UserDto} containing user details.
     * @return a {@link UserDto} representing the created user.
     */
    public UserDto create(UserDto userDto) {
        log.info("Creating new user: {}", userDto.getUsername());
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        return UserDto.fromEntity(savedUser);
    }

    /**
     * Updates an existing user.
     *
     * @param userId  the unique ID of the user to update.
     * @param userDto the {@link UserDto} containing updated user details.
     * @return a {@link UserDto} representing the updated user.
     * @throws RuntimeException if the user is not found.
     */
    public UserDto update(UUID userId, UserDto userDto) {
        log.info("Updating user with ID: {}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        if (userDto.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated with ID: {}", updatedUser.getId());
        return UserDto.fromEntity(updatedUser);
    }

    /**
     * Deletes a user by their unique ID.
     *
     * @param userId the unique ID of the user to delete.
     * @throws RuntimeException if the user is not found.
     */
    public void delete(UUID userId) {
        log.info("Deleting user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
        log.info("User deleted with ID: {}", userId);
    }
}