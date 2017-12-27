"""Backblaze B2 cloud storage for junit files."""
import aiohttp
import hashlib
from urllib.parse import urljoin


class B2Store:
    """Sotrage backed by Backblaze B2 cloud storage."""

    def __init__(self, bucket_id, bucket_name, b2_id, b2_secret):
        """Create instance of B2Store.

        Args:
            bucket_id (str): Id not name of bucket used to store files.
            bucket_name (str):NName of bucket used to store files.
            b2_id (str): Account id owning B2 bucket.
            b2_secret (str): Secret for acessing B2 bucket.
        """
        self._bucket_id = bucket_id
        self._bucket_name = bucket_name
        self._base_url = 'https://api.backblazeb2.com/b2api/v1/'
        self._auth = aiohttp.BasicAuth(b2_id, b2_secret)

    def content_sha1(self, content):
        """Return sha1 hash of content."""
        # TODO: This could be async if the content was async as well.
        return hashlib.sha1(content.encode('utf-8')).hexdigest()

    def file_name(self, owner, repo, commit):
        """Return B2 file name based on owner, repo and commit."""
        return '{}/{}/{}'.format(owner, repo, commit)

    def upload_headers(self, file_name, content, token):
        """Return headers for uploading a file to B2."""
        return {
            'Authorization': token,
            'Content-Type': 'application/xml',
            'X-Bz-File-Name': file_name,
            'X-Bz-Content-Sha1': self.content_sha1(content)
        }

    async def get_authorization_token(self):
        """Get an authorization token required for further actions.

        Returns:
            api url (str): New url to call.
            token (str): Authorization token to use for call.
        """
        # TODO: reuse session if possible.
        async with aiohttp.ClientSession(auth=self._auth) as session:
            url = urljoin(self._base_url, 'b2_authorize_account')
            async with session.get(url) as response:
                response.raise_for_status()
                auth = await response.json()
                return auth['apiUrl'], auth['authorizationToken']

    async def get_upload_url(self, api_url, token):
        """Get upload url from B2.

        Use store() or upload() directly.
        """
        headers = {'Authorization': token}
        async with aiohttp.ClientSession(headers=headers) as session:
            url = urljoin(api_url, 'b2api/v1/b2_get_upload_url')
            data = {'bucketId': self._bucket_id}
            async with session.post(url, json=data) as response:
                response.raise_for_status()
                upload_details = await response.json()
                return upload_details['uploadUrl'], upload_details['authorizationToken']

    async def upload(self, file_name, content, upload_url, token):
        """Upload content with file name to url."""
        headers = self.upload_headers(file_name, content, token)
        async with aiohttp.ClientSession(headers=headers) as session:
            async with session.post(upload_url, data=content) as response:
                response.raise_for_status()

    async def download(self, api_url, file_name, token):
        """Download content of file from bucket.

        Returns
            Content as string.

        """
        headers = {'Authorization': token}
        async with aiohttp.ClientSession(headers=headers) as session:
            url = urljoin(api_url, 'file/{}/{}'.format(self._bucket_name, file_name))
            async with session.get(url) as response:
                if response.status == 404:
                    return None

                response.raise_for_status()
                return await response.text()

    async def get(self, owner, repo, commit):
        """Load B2 file 'commit' in bucket/owner/repo.

        Returns:
            None if file was not found.
            Content of file.

        """
        api_url, token = await self.get_authorization_token()
        file_name = self.file_name(owner, repo, commit)
        return await self.download(api_url, file_name, token)

    async def store(self, owner, repo, commit, content):
        """Store content to B2 file 'commit' in bucket/owner/repo."""
        api_url, token = await self.get_authorization_token()
        upload_url, token = await self.get_upload_url(api_url, token)
        await self.upload(self.file_name(owner, repo, commit), content, upload_url, token)
