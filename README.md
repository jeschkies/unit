[![Build Status](https://travis-ci.org/jeschkies/unit.svg?branch=master)](https://travis-ci.org/jeschkies/unit) [![Coverage Status](https://coveralls.io/repos/github/jeschkies/unit/badge.svg?branch=master&service=github)](https://coveralls.io/github/jeschkies/unit)

# Unit.fm
Put your JUnit files to work for greater good!

## Development

Unit.fm is build with Gradle and requires the JDK. Call `./gradlew help` to see all details.

## Configuration

Unit.fm is configured through the following environment variables

* `UNITFM_ENV` defines whether the app runs in `DEV` or `PRO`. It defaults to `PROD`.
* `UNITFM_SECRET` defines the secret required to post a unit file.
* `GITHUB_ID` and `GITHUB_PEM` or`GITHUB_USER` and `GITHUB_TOKEN` define the Github credentials used
    for the API client.
