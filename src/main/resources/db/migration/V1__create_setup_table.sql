CREATE TABLE setup (
   id UUID PRIMARY KEY,
   tenant_name VARCHAR(255),
   is_setup_complete BOOLEAN NOT NULL DEFAULT FALSE,
   created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
   files_root VARCHAR(255),
   is_first_admin_initialized BOOLEAN NOT NULL DEFAULT FALSE,
   is_files_root_initialized BOOLEAN NOT NULL DEFAULT FALSE
);