CREATE TABLE mailjet_configuration (
   config_id UUID PRIMARY KEY,
   api_key VARCHAR(255) NOT NULL,
   api_secret VARCHAR(255) NOT NULL,
   CONSTRAINT fk_mailjet_config FOREIGN KEY (config_id)
   REFERENCES email_configuration(id) ON DELETE CASCADE
);