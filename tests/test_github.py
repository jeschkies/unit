"""Test Github API client."""
import pytest
from unitfm.github import (AccessTokenSession, BasicAuthenticatedSession, AccessTokenSessionManager)


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
    auth = AccessTokenSessionManager(key, 1234)
    assert auth._generate_jwt(1515102490, 300) == jwt


def test_headers(key):
    """Verify that request headers are correct."""
    auth = AccessTokenSessionManager(key, 1234)
    headers = auth._headers('some_jwt')
    assert headers['Authorization'] == 'Bearer some_jwt'
    assert headers['Accept'] == 'application/vnd.github.machine-man-preview+json'


def test_access_token_url(key):
    """Verify that access token request url is correct."""
    auth = AccessTokenSessionManager(key, 1234)
    url = auth._access_token_url(42)
    assert url == 'https://api.github.com/installations/42/access_tokens'


async def test_basic_authentication():
    """Verify basic authentication header."""
    session = BasicAuthenticatedSession('jeschkies', 'super secret')
    header = await session.auth_header_value()
    assert header == 'Basic amVzY2hraWVzOnN1cGVyIHNlY3JldA=='


async def test_access_token_session():
    """Verify access token header."""
    session = AccessTokenSession('super token')
    header = await session.auth_header_value()
    assert header == 'token super token'
