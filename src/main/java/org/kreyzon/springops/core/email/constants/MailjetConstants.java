package org.kreyzon.springops.core.email.constants;

import org.kreyzon.springops.core.email.enums.SmtpSecurity;

import java.util.Map;

/**
 * Constants for Mailjet email service configuration.
 * This class contains the host, ports for different security protocols,
 * and the protocol used for SMTP.
 * @author Domenico Ferraro
 */
public class MailjetConstants {
    public static String MAILJET_HOST = "in-v3.mailjet.com";
    public static Map<SmtpSecurity, Integer> MAILJET_PORTS = Map.of(
        SmtpSecurity.STARTTLS, 587,
        SmtpSecurity.SSL, 465,
        SmtpSecurity.NONE, 25
    );

    public static String PROTOCOL_SMTP = "smtp";



}
