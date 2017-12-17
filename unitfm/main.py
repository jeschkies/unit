import aiohttp
import os
from aiohttp import web
import xml.etree.ElementTree as ET

gh_user = os.environ.get('GITHUB_USER', None)
gh_token = os.environ.get('GITHUB_TOKEN', None)

# API Secret to start dogfooding.
unitfm_secret = os.environ.get('UNITFM_SECRET', None)


async def index(request):
    return web.Response(text='Hello, world!')


async def update_commit_status(owner, repo, sha, success):
    auth = aiohttp.BasicAuth(gh_user, gh_token)
    async with aiohttp.ClientSession(auth=auth) as session:
        url = 'https://api.github.com/repos/{}/{}/statuses/{}'.format(owner, repo, sha)
        data = {
            'state': 'success' if success else 'failure',
            'target_url': 'http://localhost:8000/{}/{}/commit/{}'.format(owner, repo, sha),
            'context': 'test/unit'
        }
        async with session.post(url, json=data) as response:
            response.raise_for_status()


async def post_junit(request):
    # Check if call is authorized
    provided_secret = request.query.get('secret')
    if provided_secret != unitfm_secret:
        return web.Response(status=403)

    # Process junit file
    body = await request.text()  # TODO: Parse with streaming.
    junit = ET.fromstring(body)
    failures = int(junit.get('failures'))
    success = failures == 0

    # Update commit status
    owner = request.match_info.get('owner')
    repo = request.match_info.get('repo')
    commit_sha = request.match_info.get('sha')
    await update_commit_status(owner, repo, commit_sha, success)

    return web.Response(status=201)


def app():
    app_ = web.Application()
    app_.router.add_get('/', index)
    app_.router.add_post('/{owner}/{repo}/commit/{sha}', post_junit)
    return app_


def main():
    web.run_app(app())


if __name__ == '__main__':
    main()
