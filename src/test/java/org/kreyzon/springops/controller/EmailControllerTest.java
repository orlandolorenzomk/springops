package org.kreyzon.springops.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.kreyzon.springops.auth.service.UserService;
import org.kreyzon.springops.common.dto.auth.UserDto;
import org.kreyzon.springops.common.dto.email.MailDto;
import org.kreyzon.springops.core.email.entity.SmtpConfiguration;
import org.kreyzon.springops.core.email.enums.EmailProvider;
import org.kreyzon.springops.core.email.enums.SmtpSecurity;
import org.kreyzon.springops.core.email.factory.MailSenderFactory;
import org.kreyzon.springops.core.email.request.MailjetConfigurationRequest;
import org.kreyzon.springops.core.email.request.SmtpConfigurationRequest;
import org.kreyzon.springops.core.email.service.SmtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled
@Slf4j
class EmailControllerTest {

    private static final String BASE_URL = "/email";
    private static final String SMTP_NAME = "Test SMTP Config";
    private static final String MAILJET_NAME = "Test Mailjet Config";
    private static final String SMTP_HOST = "smtp.test.com";
    private static final String MAILJET_API_KEY = "test-api-key";
    private static final String MAILJET_API_SECRET = "test-api-secret";
    private static final String SMTP_USERNAME = "testuser";
    private static final String SMTP_PASSWORD = "testpass";
    private static final String USER_EMAIL = "springops@kreyzon.com";
    private static final String USER_PASSWORD = "xayHIs4V";
    private static final String USER_NAME = "springops";
    private static final int DEFAULT_SMTP_PORT = 587;
    private static final String PROTOCOL_SMTP = "smtp";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String BEARER_TOKEN;
    private static String smtpConfigId;
    private static String mailjetConfigId;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private MailSenderFactory mailSenderFactory;

    @Autowired
    private SmtpService smtpService;

    static PostgreSQLContainer<?> postgresContainer;

    private static String MAILTRAP_HOST;
    private static int MAILTRAP_PORT;
    private static String MAILTRAP_USERNAME;
    private static String MAILTRAP_PASSWORD;

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

        MAILTRAP_HOST = System.getenv("MAILTRAP_HOST");
        MAILTRAP_PORT = Integer.parseInt(System.getenv("MAILTRAP_PORT"));
        MAILTRAP_USERNAME = System.getenv("MAILTRAP_USERNAME");
        MAILTRAP_PASSWORD = System.getenv("MAILTRAP_PASSWORD");
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
    @DisplayName("Should authenticate user")
    void shouldAuthenticate() throws Exception {
        // Create test user
        UserDto user = UserDto.builder()
                .username(USER_NAME)
                .email(USER_EMAIL)
                .password(USER_PASSWORD)
                .build();
        userService.create(user);

        // Authenticate and get token
        ResultActions result = mockMvc.perform(post("/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + USER_EMAIL + "\", \"password\":\"" + USER_PASSWORD + "\"}"))
                .andDo(print());

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));

        String responseBody = result.andReturn().getResponse().getContentAsString();
        BEARER_TOKEN = objectMapper.readTree(responseBody).get("token").asText();
    }

    @Test
    @Order(2)
    @DisplayName("Should create SMTP configuration and validate response")
    void shouldCreateSmtpConfiguration() throws Exception {
        // Create a complete SMTP configuration request
        ObjectNode requestJson = objectMapper.createObjectNode();
        requestJson.put("provider", EmailProvider.SMTP.name());
        requestJson.put("name", SMTP_NAME);
        requestJson.put("host", SMTP_HOST);
        requestJson.put("port", DEFAULT_SMTP_PORT);
        requestJson.put("protocol", PROTOCOL_SMTP);
        requestJson.put("useAuth", true);
        requestJson.put("username", SMTP_USERNAME);
        requestJson.put("password", SMTP_PASSWORD);
        requestJson.put("securityProtocol", "SSL");
        requestJson.put("useDebug", false);

        ResultActions result = mockMvc.perform(post(BASE_URL + "/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson.toString())
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andDo(print());

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(SMTP_NAME)))
                .andExpect(jsonPath("$.host", is(SMTP_HOST)))
                .andExpect(jsonPath("$.port", is(DEFAULT_SMTP_PORT)))
                .andExpect(jsonPath("$.protocol", is(PROTOCOL_SMTP)))
                .andExpect(jsonPath("$.useAuth", is(true)))
                .andExpect(jsonPath("$.username", is(SMTP_USERNAME)))
                .andExpect(jsonPath("$.password").doesNotExist()) // Password should not be present
                .andExpect(jsonPath("$.securityProtocol", is(SmtpSecurity.SSL.name())))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
        
        // Save ID for later tests
        String responseBody = result.andReturn().getResponse().getContentAsString();
        smtpConfigId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(3)
    @DisplayName("Should create Mailjet configuration and validate response")
    void shouldCreateMailjetConfiguration() throws Exception {
        // Create a complete Mailjet configuration request
        ObjectNode requestJson = objectMapper.createObjectNode();
        requestJson.put("provider", EmailProvider.MAILJET.name());
        requestJson.put("name", MAILJET_NAME);
        requestJson.put("apiKey", MAILJET_API_KEY);
        requestJson.put("apiSecret", MAILJET_API_SECRET);
        requestJson.put("securityProtocol", "STARTTLS");
        requestJson.put("useDebug", false);

        ResultActions result = mockMvc.perform(post(BASE_URL + "/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson.toString())
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andDo(print());

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(MAILJET_NAME)))
                .andExpect(jsonPath("$.apiKey").doesNotExist()) // API key should not be present
                .andExpect(jsonPath("$.apiSecret").doesNotExist()) // API secret should not be present
                .andExpect(jsonPath("$.securityProtocol", is(SmtpSecurity.STARTTLS.name())))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
                
        // Save ID for later tests
        String responseBody = result.andReturn().getResponse().getContentAsString();
        mailjetConfigId = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(4)
    @DisplayName("Should validate all SMTP configuration constraints")
    void shouldValidateAllSmtpConstraints() throws Exception {
        // Create request missing multiple required fields
        ObjectNode requestJson = objectMapper.createObjectNode();
        requestJson.put("provider", EmailProvider.SMTP.name());
        // Missing name (required)
        // Missing host (required)
        requestJson.put("port", 99999); // Invalid port (out of range)
        // Missing protocol (required)
        requestJson.put("description", "a".repeat(501)); // Too long description

        mockMvc.perform(post(BASE_URL + "/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson.toString())
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.host").exists())
                .andExpect(jsonPath("$.port").exists())
                .andExpect(jsonPath("$.protocol").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @Order(5)
    @DisplayName("Should validate all Mailjet configuration constraints")
    void shouldValidateAllMailjetConstraints() throws Exception {
        // Create request missing multiple required fields
        ObjectNode requestJson = objectMapper.createObjectNode();
        requestJson.put("provider", EmailProvider.MAILJET.name());
        // Missing name (required)
        // Missing apiKey (required)
        // Missing apiSecret (required)
        requestJson.put("description", "a".repeat(501)); // Too long description

        mockMvc.perform(post(BASE_URL + "/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson.toString())
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.apiKey").exists())
                .andExpect(jsonPath("$.apiSecret").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @Order(6)
    @DisplayName("Should get SMTP configuration by ID")
    void shouldGetSmtpConfigurationById() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + smtpConfigId)
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(smtpConfigId)))
                .andExpect(jsonPath("$.name", is(SMTP_NAME)))
                .andExpect(jsonPath("$.host", is(SMTP_HOST)))
                .andExpect(jsonPath("$.port", is(DEFAULT_SMTP_PORT)))
                .andExpect(jsonPath("$.protocol", is(PROTOCOL_SMTP)))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @Order(7)
    @DisplayName("Should get Mailjet configuration by ID")
    void shouldGetMailjetConfigurationById() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + mailjetConfigId)
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(mailjetConfigId)))
                .andExpect(jsonPath("$.name", is(MAILJET_NAME)))
                .andExpect(jsonPath("$.apiKey").doesNotExist())
                .andExpect(jsonPath("$.apiSecret").doesNotExist());
    }

    @Test
    @Order(8)
    @DisplayName("Should update SMTP configuration")
    void shouldUpdateSmtpConfiguration() throws Exception {
        // Updated values
        String updatedName = "Updated SMTP Config";
        String updatedHost = "updated-smtp.example.com";
        int updatedPort = 465;
        String updatedProtocol = "smtps";
        
        // Create update request
        ObjectNode updateRequest = objectMapper.createObjectNode();
        updateRequest.put("provider", EmailProvider.SMTP.name());
        updateRequest.put("name", updatedName);
        updateRequest.put("host", updatedHost);
        updateRequest.put("port", updatedPort);
        updateRequest.put("protocol", updatedProtocol);
        updateRequest.put("useAuth", true);
        updateRequest.put("username", "updated-user");
        updateRequest.put("password", "updated-password");
        updateRequest.put("securityProtocol", "TLS");
        
        // Perform update
        mockMvc.perform(patch(BASE_URL + "/update/" + smtpConfigId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest.toString())
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(smtpConfigId)))
                .andExpect(jsonPath("$.name", is(updatedName)))
                .andExpect(jsonPath("$.host", is(updatedHost)))
                .andExpect(jsonPath("$.port", is(updatedPort)))
                .andExpect(jsonPath("$.protocol", is(updatedProtocol)))
                .andExpect(jsonPath("$.securityProtocol", is("TLS")))
                .andExpect(jsonPath("$.password").doesNotExist());
        
        // Verify update with GET request
        mockMvc.perform(get(BASE_URL + "/" + smtpConfigId)
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updatedName)))
                .andExpect(jsonPath("$.host", is(updatedHost)));
    }

    @Test
    @Order(9)
    @DisplayName("Should update Mailjet configuration")
    void shouldUpdateMailjetConfiguration() throws Exception {
        // Updated values
        String updatedName = "Updated Mailjet Config";
        String updatedDescription = "Updated Mailjet description";
        
        // Create update request
        ObjectNode updateRequest = objectMapper.createObjectNode();
        updateRequest.put("provider", EmailProvider.MAILJET.name());
        updateRequest.put("name", updatedName);
        updateRequest.put("description", updatedDescription);
        updateRequest.put("apiKey", "updated-api-key");
        updateRequest.put("apiSecret", "updated-api-secret");
        updateRequest.put("securityProtocol", "SSL");
        
        // Perform update
        mockMvc.perform(patch(BASE_URL + "/update/" + mailjetConfigId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest.toString())
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(mailjetConfigId)))
                .andExpect(jsonPath("$.name", is(updatedName)))
                .andExpect(jsonPath("$.description", is(updatedDescription)))
                .andExpect(jsonPath("$.securityProtocol", is("SSL")))
                .andExpect(jsonPath("$.apiKey").doesNotExist())
                .andExpect(jsonPath("$.apiSecret").doesNotExist());
        
        // Verify update with GET request
        mockMvc.perform(get(BASE_URL + "/" + mailjetConfigId)
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updatedName)))
                .andExpect(jsonPath("$.description", is(updatedDescription)));
    }

    @Test
    @Order(10)
    @DisplayName("Should handle validation errors during update")
    void shouldValidateUpdateRequest() throws Exception {
        // Invalid update request (empty name, invalid port)
        ObjectNode invalidUpdateRequest = objectMapper.createObjectNode();
        invalidUpdateRequest.put("provider", EmailProvider.SMTP.name());
        invalidUpdateRequest.put("name", ""); // Empty name
        invalidUpdateRequest.put("port", 70000); // Invalid port
        
        mockMvc.perform(patch(BASE_URL + "/update/" + smtpConfigId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUpdateRequest.toString())
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.port").exists());
    }
    
    @Test
    @Order(11)
    @DisplayName("Should handle update of non-existent configuration")
    void shouldHandleNonExistentConfigUpdate() throws Exception {
        String nonExistentId = UUID.randomUUID().toString();
        
        ObjectNode validUpdateRequest = objectMapper.createObjectNode();
        validUpdateRequest.put("provider", EmailProvider.SMTP.name());
        validUpdateRequest.put("name", "Valid Name");
        validUpdateRequest.put("host", SMTP_HOST);
        validUpdateRequest.put("port", DEFAULT_SMTP_PORT);
        validUpdateRequest.put("protocol", PROTOCOL_SMTP);
        
        mockMvc.perform(patch(BASE_URL + "/update/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateRequest.toString())
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(12)
    @DisplayName("Should migrate from SMTP to Mailjet")
    void shouldMigrateFromSmtpToMailjet() throws Exception {
        // First create a new SMTP config to migrate from
        SmtpConfigurationRequest smtpRequest = SmtpConfigurationRequest.builder()
                .name("SMTP for Migration")
                .host(SMTP_HOST)
                .port(DEFAULT_SMTP_PORT)
                .protocol(PROTOCOL_SMTP)
                .build();
                
        String smtpResponse = mockMvc.perform(post(BASE_URL + "/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smtpRequest))
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String sourceConfigId = objectMapper.readTree(smtpResponse).get("id").asText();
        
        // Create migration request to Mailjet
        ObjectNode migrationRequest = objectMapper.createObjectNode();
        migrationRequest.put("name", "Migrated to Mailjet");
        migrationRequest.put("apiKey", "migrated-api-key");
        migrationRequest.put("apiSecret", "migrated-api-secret");
        migrationRequest.put("provider", EmailProvider.MAILJET.name());
        
        // Perform migration
        mockMvc.perform(patch(BASE_URL + "/update/" + sourceConfigId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(migrationRequest.toString())
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Migrated to Mailjet")))
                .andExpect(jsonPath("$.apiKey").doesNotExist())
                .andExpect(jsonPath("$.apiSecret").doesNotExist());
    }
    
    @Test
    @Order(13)
    @DisplayName("Should migrate from Mailjet to SMTP")
    void shouldMigrateFromMailjetToSmtp() throws Exception {
        // First create a new Mailjet config to migrate from
        MailjetConfigurationRequest mailjetRequest = MailjetConfigurationRequest.builder()
                .name("Mailjet for Migration")
                .apiKey(MAILJET_API_KEY)
                .apiSecret(MAILJET_API_SECRET)
                .build();
                
        String mailjetResponse = mockMvc.perform(post(BASE_URL + "/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mailjetRequest))
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String sourceConfigId = objectMapper.readTree(mailjetResponse).get("id").asText();
        
        // Create migration request to SMTP
        ObjectNode migrationRequest = objectMapper.createObjectNode();
        migrationRequest.put("name", "Migrated to SMTP");
        migrationRequest.put("host", "migrated-smtp.example.com");
        migrationRequest.put("port", 587);
        migrationRequest.put("protocol", PROTOCOL_SMTP);
        migrationRequest.put("username", "migrated-user");
        migrationRequest.put("password", "migrated-pass");
        migrationRequest.put("provider", EmailProvider.SMTP.name());
        
        // Perform migration
        mockMvc.perform(patch(BASE_URL + "/update/" + sourceConfigId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(migrationRequest.toString())
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Migrated to SMTP")))
                .andExpect(jsonPath("$.host", is("migrated-smtp.example.com")))
                .andExpect(jsonPath("$.port", is(587)))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
    
    @Test
    @Order(14)
    @DisplayName("Should validate migration request constraints")
    void shouldValidateMigrationRequestConstraints() throws Exception {
        // Create migration request with missing required fields
        ObjectNode invalidMigrationRequest = objectMapper.createObjectNode();
        invalidMigrationRequest.put("provider", EmailProvider.SMTP.name());
        // Missing name (required)
        // Missing host (required for SMTP)
        // Missing port (required for SMTP)
        // Missing protocol (required for SMTP)
        
        mockMvc.perform(patch(BASE_URL + "/update/" + smtpConfigId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidMigrationRequest.toString())
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.host").exists())
                .andExpect(jsonPath("$.port").exists())
                .andExpect(jsonPath("$.protocol").exists());
    }

    @Test
    @Order(15)
    @DisplayName("Should send test email with valid SMTP configuration")
    void shouldSendTestEmail() throws Exception {
        // Only run if Mailtrap credentials are available
        Assumptions.assumeTrue(MAILTRAP_HOST != null && MAILTRAP_USERNAME != null, 
            "Skipping email test as Mailtrap credentials are not available");
            
        // Create SMTP configuration with Mailtrap
        SmtpConfigurationRequest smtpConfigRequest = SmtpConfigurationRequest.builder()
                .name("Mailtrap SMTP Config")
                .host(MAILTRAP_HOST)
                .port(MAILTRAP_PORT)
                .protocol(PROTOCOL_SMTP)
                .useAuth(true)
                .username(MAILTRAP_USERNAME)
                .password(MAILTRAP_PASSWORD)
                .securityProtocol(SmtpSecurity.STARTTLS)
                .useDebug(true)
                .build();

        String response = mockMvc.perform(post(BASE_URL + "/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smtpConfigRequest))
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asText();

        // Prepare email data
        MailDto mailDto = MailDto.builder()
                .subject("Test Email from SpringOps")
                .receiver("test@example.com")
                .body("This is a test email sent from SpringOps tests.")
                .build();
        
        // Test sending email (no exception expected)
        SmtpConfiguration smtpConfig = smtpService.findById(UUID.fromString(id));
        JavaMailSender mailSender = mailSenderFactory.createJavaMailSender(smtpConfig);
        smtpService.sendEmail(mailDto, (JavaMailSenderImpl) mailSender);
    }

    @Test
    @Order(16)
    @DisplayName("Should delete SMTP configuration by ID")
    void shouldDeleteSmtpConfigurationById() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + smtpConfigId)
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isOk());

        // Verify deletion
        mockMvc.perform(get(BASE_URL + "/" + smtpConfigId)
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(17)
    @DisplayName("Should delete Mailjet configuration by ID")
    void shouldDeleteMailjetConfigurationById() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + mailjetConfigId)
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isOk());

        // Verify deletion
        mockMvc.perform(get(BASE_URL + "/" + mailjetConfigId)
                        .header("Authorization", "Bearer " + BEARER_TOKEN))
                .andExpect(status().isNotFound());
    }
}
