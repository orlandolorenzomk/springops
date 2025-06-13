CREATE TABLE deployment_status (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   deployment_id SERIAL NOT NULL,
   status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS', 'FAILURE', 'NOT_RUN')),
   message TEXT,
   type VARCHAR(20) NOT NULL CHECK (type IN ('UPDATE', 'BUILD', 'RUN')),
   logs_path TEXT,
   created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),

   CONSTRAINT fk_deployment
       FOREIGN KEY (deployment_id)
       REFERENCES deployments (id)
       ON DELETE CASCADE
);
