CREATE TABLE application_dependencies (
  application_id INTEGER NOT NULL,
  depends_on_application_id INTEGER NOT NULL,
  PRIMARY KEY (application_id, depends_on_application_id),
  CONSTRAINT fk_application
      FOREIGN KEY (application_id)
          REFERENCES applications(id)
          ON DELETE CASCADE,
  CONSTRAINT fk_depends_on_application
      FOREIGN KEY (depends_on_application_id)
          REFERENCES applications(id)
          ON DELETE CASCADE
);
