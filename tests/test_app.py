import pytest
from unitfm.main import app


@pytest.fixture
def cli(loop, test_client):
    test_app = app()
    return loop.run_until_complete(test_client(test_app))


async def test_index(cli):
    response = await cli.get('/')
    assert response.status == 200


async def test_rejection(cli):
    cli.server.app['unitfm_secret'] = 'super'
    response = await cli.post('/jeschkies/unit/commit/deadbeef?secret=incorrect')
    assert response.status == 403


async def test_bad_request(cli):
    cli.server.app['unitfm_secret'] = 'super'
    data = '<?xml version="1.0" encoding="utf-8"?><testsuite errors="0>'
    response = await cli.post('/jeschkies/unit/commit/deadbeef?secret=super', data=data)
    assert response.status == 400
    assert await response.text() == 'Could not parse junit file: unclosed token: line 1, column 38'


async def test_junit_not_found(cli):
    response = await cli.get('/jeschkies/unit/commit/unknown')
    assert response.status == 404
