"""Minimal Github client for the Unitfm Github App."""
import aiohttp
import jwt
import time


def generate_jwt(private_key, iss):
    """Generate a JWT given a private key from a PEM file and an issuer number."""
    issued_time = int(time.time())
    expiration_time = issued_time + 600  # in ten minutes
    payload = {'iat': issued_time, 'exp': expiration_time, 'iss': iss}
    jwt_bytes = jwt.encode(payload, private_key, algorithm='RS256')
    return jwt_bytes.decode('utf-8')


async def get_authorization_token(jwt, installation_id):
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


async def update_commit_status(owner, repo, sha, success, gh_token):
    """Call Github api and set commit status."""
    # auth = aiohttp.BasicAuth(gh_user, gh_token)
    headers = {'Authorization': 'token {}'.format(gh_token)}
    # TODO: Reuse sessions if possible.
    async with aiohttp.ClientSession(headers=headers) as session:
        url = 'https://api.github.com/repos/{}/{}/statuses/{}'.format(owner, repo, sha)
        data = {
            'state': 'success' if success else 'failure',
            'target_url': 'http://www.unit.fm/{}/{}/commit/{}'.format(owner, repo, sha),
            'context': 'test/unitfm'
        }
        async with session.post(url, json=data) as response:
            response.raise_for_status()
