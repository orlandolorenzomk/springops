package org.kreyzon.springops.core.email.mapper;

import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.common.utils.EncryptionUtils;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.core.email.dto.EmailConfigurationDto;
import org.kreyzon.springops.core.email.dto.MailjetConfigurationDto;
import org.kreyzon.springops.core.email.dto.SmtpConfigurationDto;
import org.kreyzon.springops.core.email.entity.EmailConfiguration;
import org.kreyzon.springops.core.email.entity.MailjetConfiguration;
import org.kreyzon.springops.core.email.entity.SmtpConfiguration;
import org.kreyzon.springops.core.email.request.MailjetConfigurationRequest;
import org.kreyzon.springops.core.email.request.SmtpConfigurationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting email configuration requests to entities and DTOs.
 * Handles encryption of sensitive fields like passwords and API keys.
 *
 * @author Domenico Ferraro
 */
@Component
@RequiredArgsConstructor
public class EmailConfigurationMapper {

    private final ApplicationConfig applicationConfig;

    public EmailConfigurationDto toDto(EmailConfiguration configuration) {
        if (configuration instanceof SmtpConfiguration smtpConfig) {
            return toDto(smtpConfig);
        } else if (configuration instanceof MailjetConfiguration mailjetConfig) {
            return toDto(mailjetConfig);
        } else {
            throw new SpringOpsException("Unsupported email configuration type", HttpStatus.BAD_REQUEST);
        }
    }

    public SmtpConfiguration fromRequest(SmtpConfigurationRequest request) {
        String encryptedPassword;
        try{
         encryptedPassword = EncryptionUtils.encrypt(request.getPassword(), applicationConfig.getSecret(),
                applicationConfig.getAlgorithm());
        } catch (Exception e) {
            throw new SpringOpsException("Failed to encrypt SMTP password", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return SmtpConfiguration.builder()
                .name(request.getName())
                .description(request.getDescription())
                .securityProtocol(request.getSecurityProtocol())
                .useDebug(request.getUseDebug())
                .host(request.getHost())
                .port(request.getPort())
                .protocol(request.getProtocol())
                .useAuth(request.getUseAuth())
                .username(request.getUsername())
                .password(encryptedPassword)
                .build();
    }

    public SmtpConfigurationDto toDto(SmtpConfiguration config) {
        return SmtpConfigurationDto.builder()
                .id(String.valueOf(config.getId()))
                .name(config.getName())
                .description(config.getDescription())
                .securityProtocol(config.getSecurityProtocol())
                .useDebug(config.getUseDebug())
                .host(config.getHost())
                .port(config.getPort())
                .protocol(config.getProtocol())
                .useAuth(config.getUseAuth())
                .username(config.getUsername())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    public MailjetConfiguration fromRequest(MailjetConfigurationRequest request) {
        String encryptedApiKey;
        String encryptedApiSecret;
       try {
             encryptedApiKey = EncryptionUtils.encrypt(request.getApiKey(), applicationConfig.getSecret(),
                    applicationConfig.getAlgorithm());
             encryptedApiSecret = EncryptionUtils.encrypt(request.getApiSecret(), applicationConfig.getSecret(),
                    applicationConfig.getAlgorithm());
        } catch (Exception e) {
            throw new SpringOpsException("Failed to encrypt Mailjet API credentials", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return MailjetConfiguration.builder()
                .name(request.getName())
                .description(request.getDescription())
                .securityProtocol(request.getSecurityProtocol())
                .useDebug(request.getUseDebug())
                .apiKey(encryptedApiKey)
                .apiSecret(encryptedApiSecret)
                .build();
    }

    public MailjetConfigurationDto toDto(MailjetConfiguration configuration) {
        return MailjetConfigurationDto.builder()
                .id(String.valueOf(configuration.getId()))
                .name(configuration.getName())
                .description(configuration.getDescription())
                .securityProtocol(configuration.getSecurityProtocol())
                .useDebug(configuration.getUseDebug())
                .createdAt(configuration.getCreatedAt())
                .updatedAt(configuration.getUpdatedAt())
                .build();
    }
}
