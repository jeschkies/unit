"""Definition of unit.fm app."""
from . import github
import aiohttp_jinja2
import jinja2
import logging
import os
import xml.etree.ElementTree as ET
from aiohttp import web
from .store import (B2Store, FileStore)


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


@aiohttp_jinja2.template('commits.html')
async def view_commits(request):
    """View all commits of a project/repo."""
    owner = request.match_info.get('owner')
    repo = request.match_info.get('repo')

    installation_id = 77439  # TODO: Retrieve installation from database.
    gh_session = await request.app['session_manager'].get_session(installation_id)
    commits = await github.list_commits(owner, repo, gh_session)

    if commits is None:
        error = 'Project {}/{} does not exist.'.format(owner, repo)
        return web.Response(status=404, text=error)

    return {'owner': owner, 'repo': repo, 'commits': commits}


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
    installation_id = 77439  # TODO: Retrieve installation from database.
    gh_session = await request.app['session_manager'].get_session(installation_id)
    await github.update_commit_status(owner, repo, commit_sha, success, gh_session)
    logging.info('Updated commit status for {}/{} {}.'.format(owner, repo, commit_sha))

    return web.Response(status=201)


def app():
    """Create and return aiohttp app."""
    env = os.environ.get('UNITFM_ENV', 'DEV')

    app_ = web.Application()

    # API Secret to start dogfooding.
    app_['unitfm_secret'] = os.environ.get('UNITFM_SECRET', None)

    # Register routes
    app_.router.add_get('/', index)
    app_.router.add_post('/{owner}/{repo}/commit/{sha}', post_junit)
    app_.router.add_get('/{owner}/{repo}/commit/{sha}', view_junit, name='view_junit')
    app_.router.add_get('/{owner}/{repo}/commits', view_commits, name='view_commits')

    aiohttp_jinja2.setup(app_, loader=jinja2.PackageLoader('unitfm', 'templates'))

    # TODO: don't forget to escape content ;)
    if env == 'DEV':
        logging.basicConfig(level=logging.DEBUG)

        app_['junits'] = FileStore('./tests/fixtures')

        # Configure Github connection.
        gh_user = os.environ.get('GITHUB_USER', 'unknow_nuser')
        gh_token = os.environ.get('GITHUB_TOKEN', 'no_token')
        app_['session_manager'] = github.BasicAuthenticatedSessionManager(gh_user, gh_token)
    elif env == 'PROD':
        logging.basicConfig(level=logging.INFO)

        # Configure B2 connection.
        # TODO: Do not hard code
        bucket_id = '9ed7fb02e1cf88d266040610'
        bucket_name = 'unitfm'
        b2_id = os.environ.get('B2_ID', None)
        b2_secret = os.environ.get('B2_SECRET', None)
        app_['junits'] = B2Store(bucket_id, bucket_name, b2_id, b2_secret)

        # Configure Github connection.
        gh_pem = os.environ.get('GITHUB_PEM', None)
        gh_id = os.environ.get('GITHUB_ID', None)
        app_['session_manager'] = github.AccessTokenSessionManager(gh_pem, gh_id)
    else:
        raise ValueError(
            'Unitfm environment {} is not supported. Valid values are DEV and PROD.'.format(env))
    return app_


if __name__ == '__main__':
    web.run_app(app())
