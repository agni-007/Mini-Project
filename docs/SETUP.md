# Setup and quality checks

Verified on: 2026-10-16, with Python 3.11, JDK 21, Gradle 8.13, Android SDK
(`/home/agnivesh/Android/Sdk`, build-tools 35/36).

## Python environment

Flower 1.40.0 requires Python `>=3.11,<4.0`; use `python3.11`.

```sh
make install        # creates .venv and installs dev + fl-server + reporting-api deps
```

This installs, among others:

| Component | Pin |
|---|---|
| fl-server | `flwr==1.40.0`, `numpy==1.26.4` |
| reporting-api | `fastapi==0.138.2`, `uvicorn==0.49.0`, `pydantic==2.14.1` |
| dev | `pytest==9.1.1`, `ruff==0.17.1`, `httpx==0.28.1` |

> `fastapi`/`uvicorn` are constrained by `flwr==1.40.0`
> (`fastapi>=0.138.0,<0.139.0`, `uvicorn>=0.49.0,<0.50.0`). Keep them aligned.

## Lint and tests

```sh
make lint           # ruff check .
make test           # pytest (fl-server + reporting-api)
```

Expected result (Phase 2): `ruff` → "All checks passed"; `pytest` → 6 passed.

### Troubleshooting: an unrelated `PYTHONPATH` (e.g. ROS)

If the shell exports a system `PYTHONPATH` (for example ROS at
`/opt/ros/jazzy/lib/python3.12/site-packages`), pytest may try to auto-load foreign
plugins and fail. The Makefile clears `PYTHONPATH` for Python targets
(`PYTHONPATH= $(VENV_PY) ...`). If you invoke pytest directly, do:

```sh
PYTHONPATH= .venv/bin/python -m pytest
```

## Android build

Prerequisite: an installed Android SDK. Set `sdk.dir` in `android-app/local.properties`
(gitignored) or export `ANDROID_HOME`.

```sh
make android-build              # ./gradlew assembleDebug
cd android-app && ./gradlew test
```

Verified output: `BUILD SUCCESSFUL`, debug APK at
`android-app/app/build/outputs/apk/debug/app-debug.apk`, unit tests pass.

## What is intentionally not here yet

- No dataset or trained model (Phase 4; dataset choice unresolved).
- No TFLite conversion (Phase 5).
- No multi-client federated round (Phase 6).
- No Android FL client (Phase 7, risk).
- No coarse-report validation or dashboard (Phases 8–9).
