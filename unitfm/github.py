"""Minimal Github client for the Unitfm Github App."""
import aiohttp
from collections import namedtuple
from datetime import timedelta
import jwt
import time


class BasicAuthenticatedSession(namedtuple('BasicAuthentication', ['user', 'token'])):
    """A Github session authenticated with user name and token."""

    async def auth_header_value(self):
        """Return authentication HTTP header."""
        return aiohttp.BasicAuth(self.user, self.token).encode()


class AccessTokenSession():
    """Representation of a session authenticated by a Github app with an access token.

    The access token is retrieved through the
    https://api.github.com/installations/{installation_id}/access_tokens endpoint.

    """

    def __init__(self, token):
        """Create access token based session.

        Args:
            token (str): The token returned by Github's access token backend.
        """
        self._token = token

    async def auth_header_value(self):
        """Return token based authentication header."""
        return 'token {}'.format(self._token)


class SessionManager():
    """Manages authenticated sessions with installed Github apps that query an access token."""

    def __init__(self, private_key, iss):
        """Create authentication.

        Args:
            private_key (str): The private key provided by Github for the app.
            iss (int): The Github app id.

        """
        self._private_key = private_key
        self._iss = iss
        self._liftetime = timedelta(minutes=5)

    def _generate_jwt(self, issued_time, lifetime_seconds):
        """Generate a JWT given a private key from a PEM file and an issuer number.

        Args:
            issued_time (int): Unix timestamp when token is issued.
            lifetime_seconds (int): The life time of the requests token in seconds.

        Returns:
            JSON Web Token

        """
        expiration_time = issued_time + lifetime_seconds
        payload = {'iat': issued_time, 'exp': expiration_time, 'iss': self._iss}
        jwt_bytes = jwt.encode(payload, self._private_key, algorithm='RS256')
        return jwt_bytes.decode('utf-8')

    async def _get_authorization_token(self, jwt, installation_id):
        """Return access token for installation of app."""
        headers = {
            'Authorization': 'Bearer {}'.format(jwt),
            'Accept': 'application/vnd.github.machine-man-preview+json'
        }
        async with aiohttp.ClientSession(headers=headers) as session:
            url = 'https://api.github.com/installations/{}/access_tokens'.format(installation_id)
            async with session.post(url) as response:
                response.raise_for_status()
                return await response.json()

    async def get_session(self, installation_id):
        """Return session for specific installation."""
        issued_time = int(time.time())
        lifetime_seconds = int(self._liftetime.total_seconds())
        jwt = self._generate_jwt(issued_time, lifetime_seconds)

        # Fetch token. TODO: cache token for its lifetime.
        response = await self._get_authorization_token(jwt, installation_id)
        return AccessTokenSession(response['token'])


async def update_commit_status(owner, repo, sha, success, gh_session):
    """Call Github api and set commit status."""
    headers = {'Authorization': await gh_session.auth_header_value()}
    async with aiohttp.ClientSession(headers=headers) as session:
        url = 'https://api.github.com/repos/{}/{}/statuses/{}'.format(owner, repo, sha)
        data = {
            'state': 'success' if success else 'failure',
            'target_url': 'http://www.unit.fm/{}/{}/commit/{}'.format(owner, repo, sha),
            'context': 'test/unitfm'
        }
        async with session.post(url, json=data) as response:
            response.raise_for_status()