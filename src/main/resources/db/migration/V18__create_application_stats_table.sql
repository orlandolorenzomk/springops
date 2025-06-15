CREATE TABLE application_stats (
   id              SERIAL PRIMARY KEY,
   pid             INTEGER NOT NULL,
   application_id  INTEGER NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
   timestamp       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
   memory_mb       DOUBLE PRECISION NOT NULL,  -- process RSS in MB
   cpu_load        DOUBLE PRECISION NOT NULL,  -- process CPU (0.0–1.0)
   avail_mem_mb    DOUBLE PRECISION NOT NULL,  -- total free system memory in MB
   UNIQUE (pid, timestamp)
);

CREATE INDEX idx_pid_timestamp ON application_stats(pid, timestamp);
