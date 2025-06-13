package org.kreyzon.springops.core.email.service;

import org.kreyzon.springops.common.dto.email.MailDto;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class MailjetService extends EmailService {


    public MailjetService(JavaMailSenderImpl mailSender) {
        super(mailSender);
    }


}
