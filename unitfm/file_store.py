"""File storage for junit files."""
from pathlib import Path


class FileStore:
    """File storage following the basic interface of a get and store method."""

    def __init__(self, base):
        """Create instance of file storage.

        Args:
            base (str, path): Path to base directory of file storage.
        """
        self._base_dir = base

    def get(self, owner, repo, commit):
        """Load local file 'commit' in base/owner/repo.

        Returns:
            None if file was not found.
            Content of file.

        """
        path_to_junit = Path(self._base_dir, owner, repo, commit)
        if not path_to_junit.is_file():
            return None

        with path_to_junit.open('r') as junit_file:
            return junit_file.read()

    def store(self, owner, repo, commit, content):
        """Store content to local file 'commit' in base/owner/repot.

        Raises:
            FileNotFoundError if path was not found.

        """
        path_to_junit = Path(self._base_dir, owner, repo, commit)
        with path_to_junit.open('w') as junit_file:
            junit_file.write(content)
