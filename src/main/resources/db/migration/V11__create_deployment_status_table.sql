CREATE TABLE deployment_status (
   id UUID PRIMARY KEY DEFAULT (
       (
           lpad(trunc(random()*1e8)::text, 8, '0') || '-' ||
           lpad(trunc(random()*1e4)::text, 4, '0') || '-' ||
           lpad(trunc(random()*1e4)::text, 4, '0') || '-' ||
           lpad(trunc(random()*1e4)::text, 4, '0') || '-' ||
           lpad(trunc(random()*1e12)::text, 12, '0')
           )::uuid
       ),
   status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS', 'FAILURE', 'NOT_RUN')),
   message TEXT,
   type VARCHAR(20) NOT NULL CHECK (type IN ('UPDATE', 'BUILD', 'RUN')),
   logs_path TEXT,
   created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);
