CREATE TYPE teststatus AS ENUM ('success', 'failure', 'skipped');
CREATE TABLE testcases (
    id SERIAL PRIMARY KEY,
    commit text NOT NULL ,
    repository text NOT NULL ,
    status teststatus NOT NULL,
    name text NOT NULL
);