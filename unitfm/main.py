import aiohttp
import aiohttp_jinja2
import jinja2
import os
import xml.etree.ElementTree as ET
from aiohttp import web

gh_user = os.environ.get('GITHUB_USER', None)
gh_token = os.environ.get('GITHUB_TOKEN', None)

# API Secret to start dogfooding.
unitfm_secret = os.environ.get('UNITFM_SECRET', None)


@aiohttp_jinja2.template('index.html')
async def index(request):
    return {'name': 'world'}


async def update_commit_status(owner, repo, sha, success):
    auth = aiohttp.BasicAuth(gh_user, gh_token)
    async with aiohttp.ClientSession(auth=auth) as session:
        url = 'https://api.github.com/repos/{}/{}/statuses/{}'.format(owner, repo, sha)
        data = {
            'state': 'success' if success else 'failure',
            'target_url': 'http://www.unit.fm/{}/{}/commit/{}'.format(owner, repo, sha),
            'context': 'test/unit'
        }
        async with session.post(url, json=data) as response:
            response.raise_for_status()


@aiohttp_jinja2.template('junit.html')
async def view_junit(request):
    owner = request.match_info.get('owner')
    repo = request.match_info.get('repo')
    # commit_sha = request.match_info.get('sha')

    # TODO: load correct junit xml
    junit = ET.parse('pytest.xml').getroot()
    failures = int(junit.get('failures'))
    tests = int(junit.get('tests'))

    testsuite = next(junit.iter('testsuite'))
    testcases = (case.get('name') for case in testsuite.iter('testcase'))

    return {'owner': owner, 'repo': repo, 'failures': failures, 'tests_count':
            tests, 'testcases': testcases}


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
    app_.router.add_get('/{owner}/{repo}/commit/{sha}', view_junit)

    aiohttp_jinja2.setup(app_, loader=jinja2.PackageLoader('unitfm', 'templates'))

    return app_


def main():
    web.run_app(app())


if __name__ == '__main__':
    main()
