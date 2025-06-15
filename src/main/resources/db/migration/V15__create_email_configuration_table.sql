
CREATE TABLE email_configuration (
    id UUID PRIMARY KEY  DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    security_protocol VARCHAR(20) NOT NULL CHECK (security_protocol IN ('NONE', 'SSL', 'TLS', 'STARTTLS')),
    description TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    use_debug BOOLEAN NOT NULL,
    email_provider VARCHAR(20) NOT NULL CHECK (email_provider IN ('SMTP', 'MAILJET'))
);

CREATE TABLE mailjet_configuration (
    config_id UUID PRIMARY KEY,
    api_key VARCHAR(255) NOT NULL,
    api_secret VARCHAR(255) NOT NULL,
    CONSTRAINT fk_mailjet_config FOREIGN KEY (config_id)
        REFERENCES email_configuration(id) ON DELETE CASCADE
);

CREATE TABLE smtp_config (
    config_id UUID PRIMARY KEY,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    protocol VARCHAR(50) NOT NULL,
    use_auth BOOLEAN NOT NULL,
    username VARCHAR(255),
    password VARCHAR(255),
    CONSTRAINT fk_smtp_config FOREIGN KEY (config_id)
        REFERENCES email_configuration(id) ON DELETE CASCADE
);
