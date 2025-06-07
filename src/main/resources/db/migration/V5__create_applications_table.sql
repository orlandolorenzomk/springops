CREATE TABLE applications (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  description TEXT,
  folder_root VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  git_project_https_url VARCHAR(255) NOT NULL,
  mvn_system_version_id INT REFERENCES system_versions(id) ON DELETE SET NULL,
  java_system_version_id INT REFERENCES system_versions(id) ON DELETE SET NULL
);