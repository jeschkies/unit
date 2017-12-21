"""Definition of unit.fm app."""
import aiohttp
import aiohttp_jinja2
import jinja2
import os
import xml.etree.ElementTree as ET
from aiohttp import web


@aiohttp_jinja2.template('index.html')
async def index(request):
    """Index of unit.fm."""
    return {'name': 'world'}


async def update_commit_status(owner, repo, sha, success, gh_user, gh_token):
    """Call Github api and set commit status."""
    auth = aiohttp.BasicAuth(gh_user, gh_token)
    async with aiohttp.ClientSession(auth=auth) as session:
        url = f'https://api.github.com/repos/{owner}/{repo}/statuses/{sha}'
        data = {
            'state': 'success' if success else 'failure',
            'target_url': f'http://www.unit.fm/{owner}/{repo}/commit/{sha}',
            'context': 'test/unit'
        }
        async with session.post(url, json=data) as response:
            response.raise_for_status()


@aiohttp_jinja2.template('junit.html')
async def view_junit(request):
    """View junit report."""
    owner = request.match_info.get('owner')
    repo = request.match_info.get('repo')
    commit_sha = request.match_info.get('sha')

    # Get junit file
    # TODO: use proper datastore
    if commit_sha not in request.app['junits']:
        error = f'No unit file for commit {commit_sha} found in project {owner}/{commit_sha}'
        raise web.HTTPNotFound(text=error)

    raw_junit = request.app['junits'][commit_sha]

    junit = ET.fromstring(raw_junit)
    failures = int(junit.get('failures'))
    tests = int(junit.get('tests'))

    def process_testcase(case):
        failures = list(case.iter('failure'))
        failure_text = '/n'.join(failure.text for failure in failures)
        failure_message = '/n'.join(failure.get('message') for failure in failures)
        return {
            'name': case.get('name'),
            'passed': len(failures) == 0,
            'failure_text': failure_text,
            'failure_message': failure_message
        }

    testsuite = next(junit.iter('testsuite'))
    testcases = (process_testcase(case) for case in testsuite.iter('testcase'))

    return {
        'owner': owner,
        'repo': repo,
        'failures': failures,
        'tests_count': tests,
        'testcases': testcases
    }


async def post_junit(request):
    """Post junit report."""
    # Check if call is authorized
    provided_secret = request.query.get('secret')
    if provided_secret != request.app['unitfm_secret']:
        return web.Response(status=403)

    owner = request.match_info.get('owner')
    repo = request.match_info.get('repo')
    commit_sha = request.match_info.get('sha')

    # Process junit file
    body = await request.text()  # TODO: Parse with streaming.
    try:
        junit = ET.fromstring(body)
    except ET.ParseError as e:
        error = 'Could not parse junit file: {}'.format(e)
        return web.Response(status=400, text=error)

    failures = int(junit.get('failures'))
    success = failures == 0

    # Save junit file
    # TODO: use proper datastore
    request.app['junits'][commit_sha] = body

    # Update commit status
    await update_commit_status(owner, repo, commit_sha, success, request.app['gh_user'],
                               request.app['gh_token'])

    return web.Response(status=201)


def app():
    """Create and return aiohttp app."""
    app_ = web.Application()

    app_['gh_user'] = os.environ.get('GITHUB_USER', None)
    app_['gh_token'] = os.environ.get('GITHUB_TOKEN', None)

    # API Secret to start dogfooding.
    app_['unitfm_secret'] = os.environ.get('UNITFM_SECRET', None)

    # Register routes
    app_.router.add_get('/', index)
    app_.router.add_post('/{owner}/{repo}/commit/{sha}', post_junit)
    app_.router.add_get('/{owner}/{repo}/commit/{sha}', view_junit)

    aiohttp_jinja2.setup(app_, loader=jinja2.PackageLoader('unitfm', 'templates'))

    # TODO: Save junit files in datastore
    # And don't forget to escape ;)
    app_['junits'] = dict()

    return app_


if __name__ == '__main__':
    web.run_app(app())
