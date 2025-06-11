package org.kreyzon.springops.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.auth.model.User;
import org.kreyzon.springops.auth.repository.UserRepository;
import org.kreyzon.springops.common.dto.auth.UserDto;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Finds a user by their unique ID.
     *
     * @param userId the unique ID of the user to find.
     * @return a {@link UserDto} representing the user.
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the user is not found.
     */
    public UserDto findById(UUID userId) {
        log.info("Fetching user with ID: {}", userId);
        return userRepository.findById(userId)
                .map(UserDto::fromEntity)
                .orElseThrow(() -> new SpringOpsException("User not found with ID: " + userId, HttpStatus.NOT_FOUND));
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
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the user is not found.
     */
    public UserDto update(UUID userId, UserDto userDto) {
        log.info("Updating user with ID: {}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new SpringOpsException("User not found with ID: " + userId, HttpStatus.NOT_FOUND));

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
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the user is not found.
     */
    public void delete(UUID userId) {
        log.info("Deleting user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new SpringOpsException("User not found", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(userId);
        log.info("User deleted with ID: {}", userId);
    }

    /**
     * Finds a user by their email address.
     *
     * @param email the email address of the user to load.
     * @return a {@link UserDetails} object containing user information for authentication.
     * @throws UsernameNotFoundException if no user is found with the given email address.
     */
    public User findByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = findByEmail(email);
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}