[![Build Status](https://travis-ci.org/jeschkies/unit.svg?branch=master)](https://travis-ci.org/jeschkies/unit) [![Coverage Status](https://coveralls.io/repos/github/jeschkies/unit/badge.svg?branch=master&service=github)](https://coveralls.io/github/jeschkies/unit)

# Unit.fm
Unit.fm is an Overview Page for your Unit Tests Results.

## Development

Unit.fm is build with Make and require Python 3.6+ and pip. Call `make help` to see all details.

## Configuration

Unit.fm is configured through the following environment variables

* `UNITFM_ENV` defines whether the app runs in `DEV` or `PRO`. It defaults to `PROD`.
* `UNITFM_SECRET` defines the secret required to post a unit file.
* `GITHUB_ID` and `GITHUB_PEM` or`GITHUB_USER` and `GITHUB_TOKEN` define the Github credentials used
    for the API client.
* `B2_ID` and `B2_SECRET` define the Blackblaze B2 client.
