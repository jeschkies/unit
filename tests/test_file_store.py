"""Tests for file storage."""
import pytest
import time
from unitfm.file_store import FileStore


@pytest.fixture
def storage():
    """Create and provide file storage as fixture."""
    return FileStore('./tests/fixtures')


async def test_file_not_found(storage):
    """Verify that storage returns None for unknown file."""
    result = await storage.get('jeschkies', 'plan9', 'unknown')
    assert result is None


async def test_get_file(storage):
    """Verif that a file is loaded."""
    result = await storage.get('jeschkies', 'plan9', 'empty')
    assert result.startswith('<?xml version="1.0" encoding="utf-8"?>')


async def test_store_file(storage):
    """Verif that a file is stored."""
    content = "foobar {}".format(time.time())
    await storage.store('jeschkies', 'plan9', 'new_file', content)

    with open('./tests/fixtures/jeschkies/plan9/new_file', 'r') as f:
        assert f.read() == content
