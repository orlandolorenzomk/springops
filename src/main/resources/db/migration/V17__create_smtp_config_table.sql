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