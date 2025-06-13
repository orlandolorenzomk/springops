package org.kreyzon.springops.core.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.email.Attachment;
import org.kreyzon.springops.common.dto.email.MailDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.Base64;

@Slf4j
public abstract class EmailService {

    private final JavaMailSenderImpl javaMailSender;

    EmailService(JavaMailSenderImpl javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(MailDto mailDto) {
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
            helper.setFrom("orlandolorenzo@kreyzon.com");

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

                        String fileName = "attachment." + getFileExtension(attachment.getFileType());
                        helper.addAttachment(fileName, new ByteArrayResource(decodedBytes));
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid Base64 for attachment: {}", attachment.getFileName(), e);
                    }
                }
            }

            // Send the email
            javaMailSender.send(message);
            log.info("Email sent successfully to: {}", mailDto.getReceiver());
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", mailDto.getReceiver(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String getFileExtension(String mimeType) {
        String extension;
        switch (mimeType) {
            case "application/pdf":
                extension = "pdf";
                break;
            case "image/png":
                extension = "png";
                break;
            case "image/jpeg":
                extension = "jpg";
                break;
            case "text/plain":
                extension = "txt";
                break;
            case "application/msword":
                extension = "doc";
                break;
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                extension = "docx";
                break;
            default:
                extension = "bin"; // Default to a generic binary file
                break;
        }
        return extension;
    }
}
