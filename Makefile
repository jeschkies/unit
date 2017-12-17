define USAGE
Unit.fm hand-crafted build system ⚙️

Commands:
  init      Install Python dependencies with pipenv
  test      Run linters, test db migrations and tests.
  serve     Run app in dev environment.
  deploy    Deploy to heroku.
endef

export USAGE
help:
	@echo "$$USAGE"

init:
	pip3 install pipenv
	pipenv install --dev --skip-lock

test:
	pipenv run yapf -irp unitfm tests
	pipenv run flake8 --max-line-length=100 unitfm tests
	pipenv run pytest --junitxml=pytest.xml --cov-config .coveragerc --cov unitfm --cov-report term

serve:
	pipenv run adev runserver unitfm/main.py

deploy:
	git push heroku master
