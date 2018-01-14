"""Tests for unit.fm."""
import json
import os
import pytest
import unitfm
from unitfm.main import app


@pytest.fixture
def cli(loop, test_client):
    """Create and return test client as fixture."""
    test_app = app()
    return loop.run_until_complete(test_client(test_app))


async def test_invalid_environment():
    """Verify that error is thrown for unsupported environment."""
    with pytest.raises(ValueError):
        os.environ['UNITFM_ENV'] = 'INVALID'
        app()

    # Reset environment.
    os.environ['UNITFM_ENV'] = 'DEV'


async def test_index(cli):
    """Test if index call is successful."""
    response = await cli.get('/')
    assert response.status == 200


async def test_rejection(cli):
    """Verify that a junit call with an invalid secret is rejected."""
    cli.server.app['unitfm_secret'] = 'super'
    response = await cli.post('/jeschkies/unit/commit/deadbeef?secret=incorrect')
    assert response.status == 403


async def test_bad_request(cli):
    """Verify that unparsable junit files return a 400 instead of 500."""
    cli.server.app['unitfm_secret'] = 'super'
    data = '<?xml version="1.0" encoding="utf-8"?><testsuite errors="0>'
    response = await cli.post('/jeschkies/unit/commit/deadbeef?secret=super', data=data)
    assert response.status == 400
    assert await response.text() == 'Could not parse junit file: unclosed token: line 1, column 38'


async def test_project_not_found(cli):
    """Verify that posting to and unknown repository returns 404 instead of 500."""
    cli.server.app['unitfm_secret'] = 'super'
    data = '<?xml version="1.0" encoding="utf-8"?><testsuite errors="0" failures="0"></testsuite>'
    response = await cli.post('/jeschkies/unknown/commit/deadbeef?secret=super', data=data)
    assert response.status == 404
    assert await response.text() == 'Project jeschkies/unknown does not exist.'


async def test_junit_not_found(cli):
    """Verify that unkown junit report requests return 404 instead of 500."""
    response = await cli.get('/jeschkies/unit/commit/unknown')
    assert response.status == 404


async def test_view_commits(cli, monkeypatch):  # NOQA D202
    """Verify that commits of project can be viewed."""

    async def github_list_commits_mock(owner, repo, session):
        # commits.json is the example JSON provided by Github.
        with open('./tests/fixtures/commits.json') as f:
            return json.load(f)

    monkeypatch.setattr(unitfm.github, 'list_commits', github_list_commits_mock)
    response = await cli.get('/jeschkies/known/commits')
    assert response.status == 200


async def test_view_commits_not_found(cli, monkeypatch):  # NOQA D202
    """Verify that unknown project returns 404 instead of 500."""

    async def github_list_commits_mock(owner, repo, session):
        return None

    monkeypatch.setattr(unitfm.github, 'list_commits', github_list_commits_mock)
    response = await cli.get('/jeschkies/unknown/commits')
    assert response.status == 404
