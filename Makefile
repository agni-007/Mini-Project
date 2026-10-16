# FedRoad-Net — developer commands
#
# Python environment uses python3.11 (Flower 1.40.0 requires >=3.11,<4.0).
# Android build uses the SDK at $(ANDROID_SDK_ROOT) or android-app/local.properties.

PYTHON ?= python3.11
VENV := .venv
VENV_PY := $(VENV)/bin/python
VENV_PIP := $(VENV)/bin/pip

.PHONY: help venv install lint format test fl-server reporting-api android-build clean

help:
	@echo "Targets:"
	@echo "  venv            Create the python3.11 virtualenv at $(VENV)"
	@echo "  install         Install dev + fl-server + reporting-api dependencies"
	@echo "  lint            Run ruff"
	@echo "  format          Run ruff format"
	@echo "  test            Run pytest (fl-server, reporting-api)"
	@echo "  fl-server       Start the Flower server on 0.0.0.0:8080"
	@echo "  reporting-api   Start the reporting API on 127.0.0.1:8000"
	@echo "  android-build   Build the debug APK (requires Android SDK)"
	@echo "  clean           Remove caches and build outputs"

venv:
	$(PYTHON) -m venv $(VENV)

install: venv
	$(VENV_PIP) install --upgrade pip
	$(VENV_PIP) install -r dev-requirements.txt -r fl-server/requirements.txt -r reporting-api/requirements.txt

lint:
	$(VENV)/bin/ruff check .

format:
	$(VENV)/bin/ruff format .

# PYTHONPATH is cleared so an unrelated system PYTHONPATH (e.g. ROS) cannot
# leak packages into this virtualenv.
test:
	PYTHONPATH= $(VENV_PY) -m pytest

fl-server:
	PYTHONPATH= $(VENV_PY) fl-server/server.py

reporting-api:
	PYTHONPATH= $(VENV)/bin/uvicorn app:app --app-dir reporting-api --host 127.0.0.1 --port 8000

android-build:
	cd android-app && ./gradlew assembleDebug

clean:
	rm -rf $(VENV) .pytest_cache .ruff_cache
	find . -type d -name __pycache__ -prune -exec rm -rf {} +
