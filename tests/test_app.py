import pytest
from unitfm.main import app


@pytest.fixture
def cli(loop, test_client):
    test_app = app()
    return loop.run_until_complete(test_client(test_app))


async def test_without_name(cli):
    response = await cli.get('/')
    assert response.status == 200
    assert await response.text() == 'Hello, Anonymous'


async def test_with_name(cli):
    response = await cli.get('/karsten')
    assert response.status == 200
    assert await response.text() == 'Hello, karsten'
