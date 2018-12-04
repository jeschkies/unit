CREATE TABLE organizations (organization_id SERIAL PRIMARY KEY, name text NOT NULL);
CREATE TABLE repositories (repository_id SERIAL PRIMARY KEY, name text NOT NULL);

/* A report of a build */
CREATE TABLE reports (
    report_id SERIAL PRIMARY KEY,
    organization_id INTEGER REFERENCES organizations(organization_id),
    repository_id INTEGER REFERENCES organizations(organization_id),
    commit_hash text NOT NULL,
    prefix text NOT NULL
);

/* Testsuite files. Basically all JUnit files of a build. */
CREATE TABLE testsuites (
    testsuite_id SERIAL PRIMARY KEY,
    report_id INTEGER REFERENCES reports(report_id),
    filename text NOT NULL,
    payload xml NOT NULL
)