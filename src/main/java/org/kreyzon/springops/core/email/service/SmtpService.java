package org.kreyzon.springops.core.email.service;

import org.kreyzon.springops.common.dto.email.MailDto;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class SmtpService extends EmailService {


    public SmtpService(JavaMailSenderImpl javaMailSender) {
        super(javaMailSender);
    }
}
