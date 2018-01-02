"""Definition of unit.fm app."""
import aiohttp_jinja2
import jinja2
import os
import xml.etree.ElementTree as ET
from aiohttp import web
from .b2_store import B2Store
from .file_store import FileStore
from .github import update_commit_status


@aiohttp_jinja2.template('index.html')
async def index(request):
    """Index of unit.fm."""
    return {'name': 'world'}


@aiohttp_jinja2.template('junit.html')
async def view_junit(request):
    """View junit report."""
    owner = request.match_info.get('owner')
    repo = request.match_info.get('repo')
    commit_sha = request.match_info.get('sha')

    # Get junit file
    raw_junit = await request.app['junits'].get(owner, repo, commit_sha)
    if raw_junit is None:
        error = 'No unit file for commit {} found in project {}/{}'.format(commit_sha, owner, repo)
        raise web.HTTPNotFound(text=error)

    # Parse junit file
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

    failures = int(junit.get('failures', 0))
    success = failures == 0

    # Save junit file
    try:
        await request.app['junits'].store(owner, repo, commit_sha, body)
    except FileNotFoundError:
        error = 'Project {}/{} does not exist.'.format(owner, repo)
        return web.Response(status=404, text=error)

    # Update commit status
    await update_commit_status(owner, repo, commit_sha, success, request.app['gh_user'],
                               request.app['gh_token'])

    return web.Response(status=201)


def app():
    """Create and return aiohttp app."""
    env = os.environ.get('UNITFM_ENV', 'DEV')

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

    # TODO: don't forget to escape content ;)
    if env == 'DEV':
        app_['junits'] = FileStore('./tests/fixtures')
    elif env == 'PROD':
        # TODO: Do not hard code
        bucket_id = '9ed7fb02e1cf88d266040610'
        bucket_name = 'unitfm'
        b2_id = os.environ.get('B2_ID', None)
        b2_secret = os.environ.get('B2_SECRET', None)
        app_['junits'] = B2Store(bucket_id, bucket_name, b2_id, b2_secret)
    else:
        raise ValueError(
            'Unitfm environment {} is not supported. Valid values are DEV and PROD.'.format(env))
    return app_


if __name__ == '__main__':
    web.run_app(app())
