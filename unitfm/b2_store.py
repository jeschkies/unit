import aiohttp
import hashlib
from urllib.parse import urljoin

class B2Store:
    """Sotrage backed by Backblaze B2 cloud storage."""

    def __init__(self, bucket_id, b2_id, b2_secret):
        """Create instance of B2Store.

        Args:
            bucket_id (str): Id not name of bucket used to store files.
            b2_id (str): Account id owning B2 bucket.
            b2_secret (str): Secret for acessing B2 bucket.
        """
        self._bucket_id = bucket_id
        self._base_url = 'https://api.backblazeb2.com/b2api/v1/'
        self._auth = aiohttp.BasicAuth(b2_id, b2_secret)

    async def get_authorization_token(self):
        """Gets an authorization token required for further actions."""
        # TODO: reuse session if possible.
        async with aiohttp.ClientSession(auth=self._auth) as session:
            url = urljoin(self._base_url, 'b2_authorize_account')
            async with session.get(url) as response:
                response.raise_for_status()
                return await response.json()

    async def get_upload_url(self, api_url, token):
        headers = {'Authorization': token}
        async with aiohttp.ClientSession(headers=headers) as session:
            url = urljoin(api_url, 'b2api/v1/b2_get_upload_url')
            data = {'bucketId': self._bucket_id}
            async with session.post(url, json=data) as response:
                response.raise_for_status()
                return await response.json()

    def content_sha1(self, content):
        # TODO: This could be async if the content was async as well.
        return hashlib.sha1(content.encode('utf-8')).hexdigest()

    async def upload(self, file_name, content, upload_url, token):
        headers = {
            'Authorization': token,
            'Content-Type': 'application/xml',
            'X-Bz-File-Name': file_name,
            'X-Bz-Content-Sha1': self.content_sha1(content)
        }
        async with aiohttp.ClientSession(headers=headers) as session:
            async with session.post(upload_url, data=content) as response:
                response.raise_for_status()

    async def store(self, owner, repo, commit, content):
        auth = await self.get_authorization_token()
        api_url = auth['apiUrl']
        token = auth['authorizationToken']

        upload_details = await self.get_upload_url(api_url, token)
        upload_url = upload_details['uploadUrl']
        token = upload_details['authorizationToken']

        file_name = '{}/{}/{}'.format(owner, repo, commit)
        await self.upload(file_name, content, upload_url, token)

