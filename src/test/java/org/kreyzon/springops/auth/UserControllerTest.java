package org.kreyzon.springops.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.kreyzon.springops.auth.model.User;
import org.kreyzon.springops.auth.repository.UserRepository;
import org.kreyzon.springops.common.dto.auth.AuthenticationRequestDto;
import org.kreyzon.springops.common.dto.auth.AuthenticationResponseDto;
import org.kreyzon.springops.common.dto.auth.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    private static String BEARER_TOKEN;
    static PostgreSQLContainer<?> postgresContainer;
    private static UserDto savedUser;
    private static final String VALID_PASSWORD = "Password1!";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    private static UserDto secondUser;
    private static String SECOND_USER_TOKEN;
    private static final String SECOND_USER_PASSWORD = "SecondPass1!";

    @BeforeAll
    static void startPostgresContainer() {
        postgresContainer = new PostgreSQLContainer<>("postgres:15.3")
                .withDatabaseName("springops_email_test")
                .withUsername("testuser")
                .withPassword("testpass");
        postgresContainer.start();

        System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgresContainer.getUsername());
        System.setProperty("spring.datasource.password", postgresContainer.getPassword());
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.flyway.url", postgresContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgresContainer::getUsername);
        registry.add("spring.flyway.password", postgresContainer::getPassword);
    }

    @AfterAll
    static void stopPostgresContainer() {
        if (postgresContainer != null) postgresContainer.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Should fail to create a user with invalid data")
    void shouldFailUserCreationDueToValidation() throws Exception {
        // Create an invalid DTO to test all validation constraints
        UserDto invalidUser = UserDto.builder()
                .username("a") // too short
                .email("invalid-email") // invalid email
                .password("123") // weak password
                .build();

        MvcResult result = mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        log.info("Validation errors: {}", responseBody);

        // Check presence of validation errors for each field
        assertTrue(responseBody.contains("username"));
        assertTrue(responseBody.contains("email"));
        assertTrue(responseBody.contains("password"));
    }

    @Test
    @Order(2)
    @DisplayName("Should successfully create a user")
    void shouldCreateUserSuccessfully() throws Exception {
        UserDto userDto = UserDto.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password(VALID_PASSWORD)
                .build();

        MvcResult result = mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("testuser@example.com")))
                .andExpect(jsonPath("$.password").doesNotExist()) // Password should not be returned
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        savedUser = objectMapper.readValue(responseBody, UserDto.class);
    }

    @Test
    @Order(3)
    @DisplayName("Should authenticate user and obtain token")
    void shouldAuthenticateAndGetToken() throws Exception {
        AuthenticationRequestDto authRequest = new AuthenticationRequestDto(
                savedUser.getEmail(),
                VALID_PASSWORD
        );

        MvcResult result = mockMvc.perform(post("/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthenticationResponseDto authResponse = objectMapper.readValue(responseBody, AuthenticationResponseDto.class);
        BEARER_TOKEN = authResponse.getToken();

        assertNotNull(BEARER_TOKEN);
        log.info("Authentication successful, token received");
    }

    @Test
    @Order(4)
    @DisplayName("Should find user by ID")
    void shouldFindUserById() throws Exception {
        mockMvc.perform(get("/users/{id}", savedUser.getId())
                        .header("Authorization", "Bearer " + BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedUser.getId().toString())))
                .andExpect(jsonPath("$.username", is(savedUser.getUsername())))
                .andExpect(jsonPath("$.email", is(savedUser.getEmail())));

        log.info("User found by ID: {}", savedUser.getId());
    }

    @Test
    @Order(5)
    @DisplayName("Should return 404 when user is not found")
    void shouldHandleUserNotFound() throws Exception {
        UUID nonExistingId = UUID.randomUUID();

        mockMvc.perform(get("/users/{id}", nonExistingId)
                        .header("Authorization", "Bearer " + BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        log.info("Correctly handled non-existing user with ID: {}", nonExistingId);
    }

    @Test
    @Order(6)
    @DisplayName("Should fail to create user with existing email")
    void shouldFailUserCreationWithExistingEmail() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(UUID.randomUUID())
                .username("differentusername")
                .email(savedUser.getEmail()) // Duplicate email
                .password(VALID_PASSWORD)
                .build();

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict());

        log.info("Correctly rejected creation with existing email");
    }

    @Test
    @Order(7)
    @DisplayName("Should fail to create user with existing username")
    void shouldFailUserCreationWithExistingUsername() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(UUID.randomUUID())
                .username(savedUser.getUsername()) // Duplicate username
                .email("different@example.com")
                .password(VALID_PASSWORD)
                .build();

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict());

        log.info("Correctly rejected creation with existing username");
    }

    @Test
    @Order(8)
    @DisplayName("Should fail to update user with invalid data")
    void shouldFailUserUpdateDueToValidation() throws Exception {
        UserDto invalidUpdateData = UserDto.builder()
                .id(savedUser.getId())
                .username("u") // too short
                .email("invalid-email") // invalid email
                .password("123") // weak password
                .build();

        mockMvc.perform(patch("/users/{id}", savedUser.getId())
                        .header("Authorization", "Bearer " + BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateData)))
                .andExpect(status().isUnprocessableEntity());

        log.info("Correctly rejected update with invalid data");
    }

    @Test
    @Order(9)
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() throws Exception {
        UserDto updateDto = UserDto.builder()
                .id(savedUser.getId())
                .username("updateduser")
                .email("updated@example.com")
                .password("NewPassword1!")
                .build();

        mockMvc.perform(patch("/users/{id}", savedUser.getId())
                        .header("Authorization", "Bearer " + BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("updateduser")))
                .andExpect(jsonPath("$.email", is("updated@example.com")));

        // Check that the user is updated in the DB
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertEquals("updateduser", updatedUser.getUsername());
        assertEquals("updated@example.com", updatedUser.getEmail());

        log.info("User updated successfully");
    }

    @Test
    @Order(10)
    @DisplayName("Should refresh token after user update")
    void shouldRefreshTokenAfterUserUpdate() throws Exception {
        AuthenticationRequestDto authRequest = new AuthenticationRequestDto(
                "updated@example.com",
                "NewPassword1!"
        );

        MvcResult result = mockMvc.perform(post("/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthenticationResponseDto authResponse = objectMapper.readValue(responseBody, AuthenticationResponseDto.class);
        BEARER_TOKEN = authResponse.getToken();

        assertNotNull(BEARER_TOKEN);
        log.info("Authentication updated, new token received");
    }

    @Test
    @Order(11)
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() throws Exception {
        mockMvc.perform(delete("/users/{id}", savedUser.getId())
                        .header("Authorization", "Bearer " + BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertFalse(userRepository.existsById(savedUser.getId()), "User should be deleted from the database");

        log.info("User deleted successfully");
    }

    @Test
    @Order(12)
    @DisplayName("Should create a second user for unauthorized tests")
    void shouldCreateSecondUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .username("seconduser")
                .email("seconduser@example.com")
                .password(SECOND_USER_PASSWORD)
                .build();

        MvcResult result = mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("seconduser")))
                .andExpect(jsonPath("$.email", is("seconduser@example.com")))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        secondUser = objectMapper.readValue(responseBody, UserDto.class);

        assertNotNull(secondUser);
        log.info("Second user created with ID: {}", secondUser.getId());
    }

    @Test
    @Order(13)
    @DisplayName("Should authenticate second user and obtain token")
    void shouldAuthenticateSecondUser() throws Exception {
        AuthenticationRequestDto authRequest = new AuthenticationRequestDto(
                secondUser.getEmail(),
                SECOND_USER_PASSWORD
        );

        MvcResult result = mockMvc.perform(post("/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthenticationResponseDto authResponse = objectMapper.readValue(responseBody, AuthenticationResponseDto.class);
        SECOND_USER_TOKEN = authResponse.getToken();

        assertNotNull(SECOND_USER_TOKEN);
        log.info("Second user authentication successful, token received");
    }

    @Test
    @Order(14)
    @DisplayName("Should return 403 when another user tries to update user")
    void shouldForbidUpdateFromUnauthorizedUser() throws Exception {
        // First, create a new user that will be the target of our test
        UserDto targetUser = UserDto.builder()
                .username("target_user")
                .email("target@example.com")
                .password("TargetPass1!")
                .build();

        MvcResult createResult = mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(targetUser)))
                .andExpect(status().isOk())
                .andReturn();

        UserDto createdTargetUser = objectMapper.readValue(createResult.getResponse().getContentAsString(), UserDto.class);

        // Now try to update the target user using the second user's token (should be forbidden)
        UserDto updateDto = UserDto.builder()
                .id(createdTargetUser.getId())
                .username("hacked_username")
                .email("hacked@example.com")
                .password("HackedPass1!")
                .build();

        mockMvc.perform(patch("/users/{id}", createdTargetUser.getId())
                        .header("Authorization", "Bearer " + SECOND_USER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());

        log.info("Correctly rejected unauthorized user update attempt");

        // Verify that user was not updated
        User unchangedUser = userRepository.findById(createdTargetUser.getId()).orElseThrow();
        assertEquals("target_user", unchangedUser.getUsername());
        assertEquals("target@example.com", unchangedUser.getEmail());
    }

    @Test
    @Order(15)
    @DisplayName("Should return 403 when another user tries to delete user")
    void shouldForbidDeleteFromUnauthorizedUser() throws Exception {
        // Use the same target user from the previous test
        UserDto targetUser = UserDto.builder()
                .username("target_to_delete")
                .email("target_delete@example.com")
                .password("TargetPass1!")
                .build();

        MvcResult createResult = mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(targetUser)))
                .andExpect(status().isOk())
                .andReturn();

        UserDto createdTargetUser = objectMapper.readValue(createResult.getResponse().getContentAsString(), UserDto.class);

        // Try to delete the target user using the second user's token (should be forbidden)
        mockMvc.perform(delete("/users/{id}", createdTargetUser.getId())
                        .header("Authorization", "Bearer " + SECOND_USER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        log.info("Correctly rejected unauthorized user delete attempt");

        // Verify that user still exists
        assertTrue(userRepository.existsById(createdTargetUser.getId()),
                "User should still exist in the database after failed delete attempt");

        // Clean up by authenticating as the target user and deleting it properly
        AuthenticationRequestDto authRequest = new AuthenticationRequestDto(
                createdTargetUser.getEmail(),
                "TargetPass1!"
        );

        MvcResult authResult = mockMvc.perform(post("/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String targetToken = objectMapper.readValue(authResult.getResponse().getContentAsString(),
                AuthenticationResponseDto.class).getToken();

        // Delete properly with the right token
        mockMvc.perform(delete("/users/{id}", createdTargetUser.getId())
                        .header("Authorization", "Bearer " + targetToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Also clean up the second user
        mockMvc.perform(delete("/users/{id}", secondUser.getId())
                        .header("Authorization", "Bearer " + SECOND_USER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}