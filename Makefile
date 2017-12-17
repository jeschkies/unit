define USAGE
Unit.fm hand-crafted build system ⚙️

Commands:
  init      Install Python dependencies with pipenv
  serve     Run app in dev environment.
  test      Run linters, test db migrations and tests.
endef

export USAGE
help:
	@echo "$$USAGE"

init:
	pip3 install pipenv
	pipenv install --dev --skip-lock

test:
	pipenv run yapf -irp unit tests
	pipenv run flake8 --max-line-length=100 unit tests
	pipenv run pytest --cov-config .coveragerc --cov unitfm --cov-report term

serve:
	pipenv run python unit/main.py
