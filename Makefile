define USAGE
Unit.fm hand-crafted build system ⚙️

Commands:
  init           Install Python dependencies with pipenv
  test           Run linters, test db migrations and tests.
  integration    Run unit and integration tests. Requires env variables B2_ID and B2_SECRET
                 to be set to B2 appliication id and key respectively.
  serve          Run app in dev environment.
  deploy         Deploy to heroku.
  report         Report own unit tests results to Github.
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
	pipenv run pytest -m "not integration" --junitxml=pytest.xml --cov-config .coveragerc --cov unitfm --cov-report term

integration:
	pipenv run yapf -irp unitfm tests
	pipenv run flake8 --max-line-length=100 unitfm tests
	pipenv run pytest --junitxml=pytest.xml --cov-config .coveragerc --cov unitfm --cov-report term

serve:
	pipenv run adev runserver unitfm/main.py

deploy:
	git push heroku master

report:
	curl -X POST -d "@pytest.xml" "http://www.unit.fm/jeschkies/unit/commit/$(TRAVIS_COMMIT)?secret=$(UNITFM_SECRET)"
