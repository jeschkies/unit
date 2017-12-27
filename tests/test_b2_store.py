"""Tests for B2 storage."""
import pytest
from unitfm.b2_store import B2Store


@pytest.fixture
def storage():
    """Create and provide B2 storage as fixture."""
    return B2Store('123', 'unitfm', 'id', 'key')


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
