"""Verify Gtihub client with actual Github API."""
import os
import pytest
from unitfm import github
from unitfm.github import BasicAuthenticatedSessionManager


@pytest.fixture
async def gh_session(loop):
    """Provide basic auth session for integration tests with Github.

    Note: No integration tests should do modifications.
    """
    gh_user = os.environ.get('GITHUB_USER', 'unknow_nuser')
    gh_token = os.environ.get('GITHUB_TOKEN', 'no_token')
    session = await BasicAuthenticatedSessionManager(gh_user, gh_token).get_session(None)
    return session


@pytest.mark.integration
async def test_list_commits(gh_session):
    """Verify listing of all commits."""
    commits = await github.list_commits('jeschkies', 'unit', gh_session)
    assert len(commits) >= 1


@pytest.mark.integration
async def test_list_commits_not_found(gh_session):
    """Verify listing of all commits returns None for unknown project."""
    commits = await github.list_commits('jeschkies', 'unkown', gh_session)
    assert commits is None
