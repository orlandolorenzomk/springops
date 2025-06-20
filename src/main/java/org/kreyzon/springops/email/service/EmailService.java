package org.kreyzon.springops.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.email.Attachment;
import org.kreyzon.springops.common.dto.email.MailDto;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.email.dto.EmailConfigurationDto;
import org.kreyzon.springops.email.entity.EmailConfiguration;
import org.kreyzon.springops.email.mapper.EmailConfigurationMapper;
import org.kreyzon.springops.email.utils.EmailUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Abstract service for sending emails.
 * Provides a method to send emails with attachments and CC recipients.
 * Handles MIME message creation and sending via JavaMailSender.
 *
 * @author Domenico Ferraro
 */
@Slf4j
@Component
@RequiredArgsConstructor
public abstract class EmailService {

    protected final EmailConfigurationMapper mapper;

    /**
     * Sends an email using the provided MailDto and JavaMailSender.
     * This method constructs a MIME message, sets the recipient, subject, body, and any attachments.
     *
     * @param mailDto the MailDto containing email details such as receiver, subject, body, attachments, and CC recipients.
     * @param javaMailSender the JavaMailSenderImpl instance used to send the email.
     */
    @Retryable(
            value = { SpringOpsException.class, MailException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000))
     public void sendEmail(MailDto mailDto, JavaMailSenderImpl javaMailSender) {
        log.info("Sending email to: {}", mailDto.getReceiver());
        log.info("Subject: {}", mailDto.getSubject());
        log.info("Body: {}", mailDto.getBody());
        log.info("Attachments: {}", mailDto.getAttachments());
        log.info("CC: {}", mailDto.getCc());

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Set the basic email details
            helper.setTo(mailDto.getReceiver());
            helper.setSubject(mailDto.getSubject());
            helper.setText(mailDto.getBody(), true); // true indicates HTML content
            helper.setFrom("springops_test@kreyzon.com");

            // Add CC recipients if any
            if (mailDto.getCc() != null && !mailDto.getCc().isEmpty()) {
                helper.setCc(mailDto.getCc().toArray(new String[0]));
            }

            // Decode and attach base64 attachments
            if (mailDto.getAttachments() != null && !mailDto.getAttachments().isEmpty()) {
                for (Attachment attachment : mailDto.getAttachments()) {
                    try {
                        byte[] decodedBytes = Base64.getDecoder().decode(attachment.getBase64());

                        // Validate that decoded bytes are not empty
                        if (decodedBytes.length == 0) {
                            log.error("Decoded attachment is empty: {}", attachment.getFileName());
                            continue;
                        }

                        String fileName = "attachment." + EmailUtils.getFileExtension(attachment.getFileType());
                        helper.addAttachment(fileName, new ByteArrayResource(decodedBytes));
                    } catch (SpringOpsException e) {
                        log.error("Invalid Base64 for attachment: {}", attachment.getFileName(), e);
                    }
                }
            }

            // Send the email
            javaMailSender.send(message);
            log.info("Email sent successfully to: {}", mailDto.getReceiver());
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", mailDto.getReceiver(), e);
            throw new SpringOpsException("Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    EmailConfigurationDto toDto(EmailConfiguration emailConfiguration) {
        return mapper.toDto(emailConfiguration);
    }
}
