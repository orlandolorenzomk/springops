ALTER TABLE applications
ADD COLUMN IF NOT EXISTS
    java_minimum_memory VARCHAR (10),
ADD COLUMN IF NOT EXISTS
    java_maximum_memory VARCHAR (10);
