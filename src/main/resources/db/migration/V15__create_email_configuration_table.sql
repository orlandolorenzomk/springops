CREATE TABLE email_configuration (
     id UUID PRIMARY KEY DEFAULT (
         (
             lpad(trunc(random()*1e8)::text, 8, '0') || '-' ||
             lpad(trunc(random()*1e4)::text, 4, '0') || '-' ||
             lpad(trunc(random()*1e4)::text, 4, '0') || '-' ||
             lpad(trunc(random()*1e4)::text, 4, '0') || '-' ||
             lpad(trunc(random()*1e12)::text, 12, '0')
             )::uuid
         ),
     name VARCHAR(255) NOT NULL,
     security_protocol VARCHAR(20) NOT NULL CHECK (security_protocol IN ('NONE', 'SSL', 'TLS', 'STARTTLS')),
     description TEXT,
     created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
     updated_at TIMESTAMP WITHOUT TIME ZONE,
     use_debug BOOLEAN NOT NULL,
     email_provider VARCHAR(20) NOT NULL CHECK (email_provider IN ('SMTP', 'MAILJET'))
);
