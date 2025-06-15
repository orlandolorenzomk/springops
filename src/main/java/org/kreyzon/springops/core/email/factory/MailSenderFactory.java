package org.kreyzon.springops.core.email.factory;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.common.utils.EncryptionUtils;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.core.email.constants.MailjetConstants;
import org.kreyzon.springops.core.email.entity.EmailConfiguration;
import org.kreyzon.springops.core.email.entity.MailjetConfiguration;
import org.kreyzon.springops.core.email.entity.SmtpConfiguration;
import org.kreyzon.springops.core.email.enums.SmtpSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Factory for creating JavaMailSender instances based on email configuration.
 * Timeout settings can be configured via application.yml, default is 5000ms.
 *
 * @author Domenico Ferraro
 */
@Component
@Slf4j
public class MailSenderFactory {

    private final ApplicationConfig applicationConfig;

    @Value("${mail.smtp.connectiontimeout:5000}")
    private int connectionTimeout;

    @Value("${mail.smtp.timeout:5000}")
    private int timeout;

    @Value("${mail.smtp.writetimeout:5000}")
    private int writeTimeout;

    public MailSenderFactory(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    /**
     * Creates a JavaMailSender based on the provided email configuration.
     *
     * @param config Email configuration (SMTP or Mailjet)
     * @return Configured JavaMailSender
     * @throws SpringOpsException with {@link HttpStatus#BAD_REQUEST} if configuration type is not supported
     */
    public JavaMailSender createJavaMailSender(EmailConfiguration config) {
        if (config == null) {
            log.info("Email configuration is null, cannot create JavaMailSender");
            throw new SpringOpsException("Email configuration cannot be null", HttpStatus.BAD_REQUEST);
        }
        if (config instanceof SmtpConfiguration) {
            return createSmtpMailSender((SmtpConfiguration) config);
        } else if (config instanceof MailjetConfiguration) {
            return createMailjetSender((MailjetConfiguration) config);
        }
        throw new SpringOpsException("Unsupported email configuration type", HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates a JavaMailSender instance for standard SMTP configuration.
     *
     * @param config SMTP configuration
     * @return Configured JavaMailSenderImpl
     * @throws SpringOpsException with {@link HttpStatus#BAD_REQUEST} if authentication is enabled but username or password is missing.
     * @throws SpringOpsException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if unable to decrypt the password.
     */
    private JavaMailSenderImpl createSmtpMailSender(SmtpConfiguration config) {
        log.info("Creating SMTP JavaMailSender with configuration: {}", config);
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        mailSender.setProtocol(config.getProtocol());

        String decryptedPassword = null;
        if (Boolean.TRUE.equals(config.getUseAuth())) {
            if (StringUtils.isBlank(config.getUsername()) || StringUtils.isBlank(config.getPassword())) {
                throw new SpringOpsException("SMTP authentication is enabled but username or password is missing", HttpStatus.BAD_REQUEST);
            }

            try {
                decryptedPassword = EncryptionUtils.decrypt(
                        config.getPassword(),
                        applicationConfig.getSecret(),
                        applicationConfig.getAlgorithm()
                );
            } catch (Exception e) {
                throw new SpringOpsException("Unable to decrypt SMTP password", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return configureMailSender(mailSender, config, config.getUsername(), decryptedPassword);
    }

    /**
     * Creates a JavaMailSender instance configured for Mailjet.
     *
     * @param config Mailjet configuration
     * @throws SpringOpsException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if unable to decrypt API credentials.
     * @return Configured JavaMailSenderImpl
     */
    private JavaMailSenderImpl createMailjetSender(MailjetConfiguration config) {
        log.info("Creating Mailjet JavaMailSender with configuration: {}", config);
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        int port = MailjetConstants.MAILJET_PORTS.get(config.getSecurityProtocol());

        mailSender.setHost(MailjetConstants.MAILJET_HOST);
        mailSender.setPort(port);
        mailSender.setProtocol(MailjetConstants.PROTOCOL_SMTP);

        String apiKey;
        String apiSecret;

        try {
            apiKey = EncryptionUtils.decrypt(config.getApiKey(), applicationConfig.getSecret(), applicationConfig.getAlgorithm());
            apiSecret = EncryptionUtils.decrypt(config.getApiSecret(), applicationConfig.getSecret(), applicationConfig.getAlgorithm());
        } catch (Exception e) {
            throw new SpringOpsException("Unable to decrypt Mailjet API credentials", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return configureMailSender(mailSender, config, apiKey, apiSecret);
    }

    /**
     * Applies common configuration to a JavaMailSender instance.
     *
     * @param mailSender JavaMailSenderImpl instance to configure
     * @param config     Email configuration (SMTP or Mailjet)
     * @param username   Username or API key
     * @param password   Password or API secret
     * @return Configured JavaMailSenderImpl
     */
    private JavaMailSenderImpl configureMailSender(JavaMailSenderImpl mailSender, EmailConfiguration config, String username, String password) {
        Properties props = mailSender.getJavaMailProperties();
        boolean useAuth = username != null && password != null;

        if (useAuth) {
            mailSender.setUsername(username);
            mailSender.setPassword(password);
        }

        props.put("mail.smtp.auth", useAuth);
        props.put("mail.debug", Boolean.TRUE.equals(config.getUseDebug()));

        // Apply security settings based on selected protocol
        configureSecurity(props, config.getSecurityProtocol(), mailSender.getPort());

        // Timeout settings
        props.put("mail.smtp.connectiontimeout", connectionTimeout);
        props.put("mail.smtp.timeout", timeout);
        props.put("mail.smtp.writetimeout", writeTimeout);
        log.info("Configured mail sender with host: {}, port: {}, protocol: {}, useAuth: {}",
                mailSender.getHost(), mailSender.getPort(), mailSender.getProtocol(), useAuth);
        return mailSender;
    }

    /**
     * Configures security settings based on the specified SMTP security protocol.
     *
     * @param props            JavaMail properties
     * @param securityProtocol Security protocol (SSL,TLS, STARTTLS, NONE)
     * @param port             SMTP port
     */
    private void configureSecurity(Properties props, SmtpSecurity securityProtocol, int port) {
        if (securityProtocol == null || securityProtocol == SmtpSecurity.NONE) {
            log.info("No security protocol specified, using default settings.");
            return;
        }

        switch (securityProtocol) {
            case SSL:
                props.put("mail.smtp.ssl.enable", true);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.port", port);
                props.put("mail.smtp.ssl.trust", "*");
                break;
            case TLS:
            case STARTTLS:
                props.put("mail.smtp.starttls.enable", true);
                props.put("mail.smtp.starttls.required", true);
                break;
            default:
                // No security configuration
                break;
        }
    }
}
