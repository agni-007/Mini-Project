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

Expected result (Phase 6): `ruff` → "All checks passed"; `pytest` → 14 passed
(including the real two-client federated round in
`fl-server/tests/test_federated_round.py`).

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

### Run on a real device (verified on-device)

With a device connected via USB debugging (`adb devices` shows it):

```sh
export ANDROID_HOME=/home/agnivesh/Android/Sdk
cd android-app && ./gradlew installDebug
$ANDROID_HOME/platform-tools/adb shell am start -n org.fedroad.net/.MainActivity
```

The activity shows a live status label. Read it non-interactively with:

```sh
$ANDROID_HOME/platform-tools/adb shell uiautomator dump /sdcard/ui.xml
$ANDROID_HOME/platform-tools/adb shell cat /sdcard/ui.xml | grep -o 'text="[^"]*"'
```

**Verified on-device (2026-10-16, Realme RMX3868, Android 14):** the app installed and
launched without crashing; the accelerometer streamed live (sample count rising
continuously); `EventWindowExtractor` emitted windows (`Last window: 128 samples`);
`Inference: not_ready` (expected, no model yet); pause/resume (Home → relaunch) worked
with no crash and no `FATAL` entries in logcat.

> Caveat: this validates the sensing/windowing/status plumbing only. "Events detected"
> during this run came from device handling, not from a validated classifier. Detection
> accuracy is still blocked on the Phase 4 dataset/model.

## What is intentionally not here yet

- No dataset or trained model (Phase 4; dataset choice unresolved).
- No TFLite conversion or real on-device model (Phase 5 scaffold only; `Inference:
  not_ready`).
- No Android federated-learning client (Phase 7, risk).
- No coarse-report validation or dashboard (Phases 8–9).
