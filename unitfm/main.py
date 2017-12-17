import aiohttp
import os
from aiohttp import web
import xml.etree.ElementTree as ET

gh_user = os.environ.get('GITHUB_USER', None)
gh_token = os.environ.get('GITHUB_TOKEN', None)

async def handle(request):
    name = request.match_info.get('name', "Anonymous")
    text = 'Hello, {}'.format(name)
    return web.Response(text=text)


async def update_commit_status(owner, repo, sha):
    auth = aiohttp.BasicAuth(gh_user, gh_token)
    async with aiohttp.ClientSession(auth=auth) as session:
        url = 'https://api.github.com/repos/{}/{}/statuses/{}'.format(owner, repo, sha)
        data = {'state': 'pending', 'target_url': 'http://localhost:8000/', 'context': 'test/unit'}
        async with session.post(url, json=data) as response:
            response.raise_for_status()

async def post_junit(request):
    junit = ET.parse('pytest.xml').getroot()
    page = 'Errors: {}'.format(junit.get('errors'))

    owner = request.match_info.get('owner', None)
    repo = request.match_info.get('repo', None)
    commit_sha = request.match_info.get('sha', None)
    await update_commit_status(owner, repo, commit_sha)

    return web.Response(status=201)


def app():
    app_ = web.Application()
    app_.router.add_get('/', handle)
    app_.router.add_post('/{owner}/{repo}/commit/{sha}', post_junit)
    return app_


def main():
    web.run_app(app())


if __name__ == '__main__':
    main()
