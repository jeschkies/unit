"""Tests for B2 storage."""
import os
import pytest
from unitfm.b2_store import B2Store


@pytest.fixture
def storage():
    """Create and provide B2 storage as fixture."""
    b2_id = os.environ.get('B2_ID', '')
    b2_secret = os.environ.get('B2_SECRET', '')
    return B2Store('67f990da46bcaddb68080d11', 'unitfm-test', b2_id, b2_secret)


def test_upload_headers(storage):
    """Verify that upload headers are complete."""
    headers = storage.upload_headers('jeschkies/plan9/deadbeef', 'foobar', 'some_token')
    assert headers['Authorization'] == 'some_token'
    assert headers['Content-Type'] == 'application/xml'
    assert headers['X-Bz-File-Name'] == 'jeschkies/plan9/deadbeef'
    assert headers['X-Bz-Content-Sha1'] == '8843d7f92416211de9ebb963ff4ce28125932878'


def test_file_name(storage):
    """Verify that filen ame is constructed."""
    file_name = storage.file_name('jeschkies', 'plan9', 'deadbeef')
    assert file_name == 'jeschkies/plan9/deadbeef'


@pytest.mark.integration
async def test_file_not_found(storage):
    """Verify that storage returns None for unknown file."""
    result = await storage.get('jeschkies', 'plan9', 'unknown')
    assert result is None


@pytest.mark.integration
async def test_get_file(storage):
    """Verif that a file is loaded."""
    result = await storage.get('jeschkies', 'plan9', 'empty')
    assert result.startswith('<?xml version="1.0" encoding="utf-8"?>')
