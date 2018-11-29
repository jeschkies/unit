/* A report of a build */
CREATE TABLE reports (
    id SERIAL PRIMARY KEY,
    commit text NOT NULL,
    key text NOT NULL /*S3 like key.*/
);

/* Testsuite files. Basically all JUnit files of a build. */
CREATE TABLE testsuites (
    id SERIAL PRIMARY KEY,
    report_id integer REFERENCES reports,
    name text,
    payload xml NOT NULL
);