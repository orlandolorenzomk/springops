CREATE TABLE deployments (
 id SERIAL PRIMARY KEY,
 application_id INT NOT NULL,
 version VARCHAR(50) NOT NULL,
 status VARCHAR(50) NOT NULL,
 pid INT,
 type VARCHAR(20) CHECK (type IN ('CURRENT', 'BACKUP')),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE
);