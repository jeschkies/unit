"""Test Github API client."""
from unitfm.github import SessionManager
import pytest


@pytest.fixture
def key():
    """Provide a fake private key."""
    with open('./tests/fixtures/fake.pem') as f:
        yield f.read()


@pytest.fixture
def jwt():
    """Provide the JWT generated with jwt.io and the private key."""
    with open('./tests/fixtures/expected.jwt') as f:
        yield f.read()


def test_jwt_generate(key, jwt):
    """Verify that JWT is correct."""
    auth = SessionManager(key, 1234)
    assert auth._generate_jwt(1515102490, 300) == jwt
