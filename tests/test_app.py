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
    response = await cli.post('/jeschkies/unit/commit/deadbeef?secret=incorrect')
    assert response.status == 403
