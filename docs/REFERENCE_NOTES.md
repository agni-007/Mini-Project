# Reference Notes — Phase 1 Reconnaissance

This document records what was **actually inspected** for Phase 1. Every claim below
was verified by cloning or reading the source in this session. Items that could not be
verified are marked **UNVERIFIED**.

Inspection date: 2026-10-16 (UTC).

---

## 1. Target repository

- URL: https://github.com/agni-007/Mini-Project.git
- `git ls-remote --heads origin` returned **no refs** (empty repository, exit code 0).
- The local workspace (`/home/agnivesh/code/mini-project`) was **not** a Git repository
  and contained only the requirements file `implementation (1).md`.
- Action taken: `git init -b main`, `git remote add origin <target URL>`.
- No existing commits, branches, or files to preserve. No remote mismatch.

## 2. Reference A — ht-eml/pothole-detection

- URL: https://github.com/ht-eml/pothole-detection.git
- Cloned to `/tmp/opencode/pothole-detection` (shallow).
- Verified HEAD commit: `e4ab980e7f5c3143ea3e69752c91badcd1072d8c` (default branch `master`).
- **License: NONE.** `git ls-files | grep -iE "licen|copying"` returned no matches.
  There is no `LICENSE`, `COPYING`, or license statement in the README. Under default
  copyright, no reuse license is granted.
  - **Decision: conceptual reference only. No code or model artifacts may be copied
    into this project without the owner's explicit permission.**

### 1.1 Verified structure (selected paths)

Repo root contains top-level directories with spaces in their names:

```
Android App Code/         (Java, single-activity TensorFlow Lite examples app)
sensor based model/       # frozen_model.pb + readme
vision based model/       # detect.tflite + labelmap.txt + readme
rails server/            # readme only: "will upload soon"
Android APK file/        # prebuilt apk + readme
pictures/
README.md
```

### 1.1.1 Sensor capture / windowing logic (verified)

File: `Android App Code/Sensors.java` (commit `e4ab980`).

- Sensor: `Sensor.TYPE_ACCELEROMETER`, `SensorManager.SENSOR_DELAY_GAME`.
- Window length: `N_SAMPLES = 200` samples **per axis**; collects `x`, `y`, `z` into
  three parallel `List<Float>`.
- Windowing is **non-overlapping and untriggered**: once each list has 200 samples the
  code concatenates `x + y + z` (600 floats), runs inference, then clears all three
  lists and starts filling again.
- No candidate-event threshold detection; the model output is thresholded after
  inference (`results[4] > 0.20f` → send location).
- Labels array is `{"", "", "", "smooth", "pothole", ""}`; the used labels are index 3
  `smooth` and index 4 `pothole`. Output size is 6.

File: `Android App Code/TensorFlowClassifier.java` (commit `e4ab980`).

- Uses deprecated `org.tensorflow.contrib.android.TensorFlowInferenceInterface` and
  native library `tensorflow_inference` (old TFLite C++ / frozen-graph path).
- Model file: `file:///android_asset/frozen_model.pb`.
- Input node `inputs`, input shape `{1, 200, 3}`, output node `y_`, output size `6`.

File: `Android App Code/LocationDelegate.java` (commit `e4ab980`).

- On detection, requests `PRIORITY_HIGH_ACCURACY` location and POSTs it to a backend
  (`Api.sendLocation`) when `location.getAccuracy() <= 0.8` metres.
- **This is a precise-GPS upload path and is explicitly NOT to be reused.** FedRoad-Net
  must keep precise coordinates on-device and, if hotspot reporting is enabled, send only
  a coarse cell id.

### 1.1.2 Model artifacts (verified presence only, not evaluated)

- `sensor based model/frozen_model.pb` exists (binary; not run or evaluated here).
- `vision based model/detect.tflite` and `labelmap.txt` exist.
- README claims LSTM with 0.24 test loss and SSD MobileNet v2 with mAP 81%. These numbers
  are **claims by the upstream author and were not reproduced**; do not cite them as our
  results.

### 1.1.3 Datasets

- README states: *"Our Dataset is Self Collected and not available Publicly."*
- No dataset files are present in the repository.
- Consequence: Phase 4 cannot source labelled accelerometer data from this repo.

### 1.1.4 License / reuse decision

- Repository has **no license file**. Under default copyright it is "all rights reserved".
- Decision: use for **conceptual reference only** (e.g. 200-sample, 3-axis window shape).
  Do **not** copy code or model binaries. This limitation is recorded in REFERENCE_NOTES
  as required by the plan.

---

## 2. Reference B — Flower (`flwrlabs/flower`)

- URL: https://github.com/flwrlabs/flower.git
- Cloned (depth 1) at commit `f8b306c5853e6f5b83a192d71f94a59eb36373d0`.
- License: `LICENSE` at repo root is **Apache License 2.0** (verified by reading the file).
- In-tree framework version: `framework/pyproject.toml` → `version = "1.41.0"`
  (development/main branch).
- `framework/pyproject.toml` → `requires-python = ">=3.11,<4.0"`.
- Released version available on PyPI at inspection time: **`flwr` 1.40.0**
  (verified with `pip index versions flwr`; latest listed = 1.40.0).

### 2.1 Android examples — still present

Both legacy Android examples still exist in the current tree:

- `examples/android/` (Java + TensorFlow)
- `examples/android-kotlin/` (Kotlin + TensorFlow Lite)

### 2.1.1 `examples/android-kotlin` (verified file contents)

Paths verified under `examples/android-kotlin/`:

- `README.md` — states the example is from 2022, uses `FedAvgAndroid`, requires a
  TFLite model with `train`/`infer`/`save`/`restore` signatures, and includes the note:
  *"the highest tested JDK version the app supports is 16; it fails to build using
  JDK 19 on macOS."* This session's environment has **JDK 21**, which is outside the
  tested range.
- `server.py` — imports `from flwr.server.strategy import FedAvgAndroid` and
  `from flwr.server import ServerConfig, start_server`.
- `requirements.txt` — `flwr>=1.0, <2.0` (unpinned upper range).
- `client/flower_tflite/src/main/proto/transport.proto` — defines the **legacy**
  `service FlowerService { rpc Join(stream ClientMessage) returns (stream ServerMessage) }`
  protocol with `Code`/`Status`/`Parameters`/`Reason`/`ServerMessage`/`ClientMessage`
  messages.
- `client/flower_tflite/src/main/java/dev/flower/flower_tflite/FlowerClient.kt` — wraps a
  TFLite `Interpreter`; calls `interpreter.runSignature(inputs, outputs, "parameters" |
  "restore" | "train" | "infer")`. This confirms the example performs **on-device
  training via TFLite signatures**, not inference only.
- `client/flower_tflite/src/main/java/dev/flower/flower_tflite/FlowerServiceRunnable.kt` —
  builds a gRPC `ManagedChannel` to the server implementing the legacy `FlowerService`.

### 2.1.2 `examples/android` (Java + TensorFlow) (verified file contents)

- `requirements.txt` — `flwr>=1.0, <2.0`, plus `tensorflow-cpu>=2.9.1, !=2.11.1`.
- `server.py` — uses `fl.server.strategy.FedAvgAndroid` and `fl.server.start_server(...)`.
- `client/app/build.gradle` — `compileSdkVersion 33`, `minSdkVersion 24`,
  AGP not pinned here but the root uses an old plugin in the sibling example; gRPC
  `1.43.0`, protobuf `3.17.3`, and `org.tensorflow:tensorflow-lite:0.0.0-nightly-SNAPSHOT`.
- `client/app/src/main/proto/transport.proto` — same legacy `FlowerService` protocol.

### 2.2 Compatibility verification (important)

In current Flower 1.41.0 tree, the symbols the Android examples depend on still exist
but are **legacy/compatibility** paths:

- `FedAvgAndroid` still exists: `framework/py/flwr/server/strategy/fedavg_android.py:44`,
  re-exported from `.../strategy/__init__.py:37`.
- `start_server` still exists but is explicitly marked deprecated in source:
  `framework/py/flwr/server/__init__.py:22`
  `from ..compat.server.app import start_server as start_server  # Deprecated`
- The **modern** server surface is SuperLink/SuperNode + `flwr run` (Drivers/Apps), and
  the current gRPC service tree includes `fleet.proto` (`service Fleet`), `control.proto`,
  `runtime.proto`, etc. The Android examples do **not** use the modern fleet protocol;
  they use the legacy `FlowerService` stream.
- Both Android examples' `README`/code still target the legacy protocol and are not part
  of any visible CI job in `.github/` (grep for `android-kotlin`/`examples/android` in
  `.github/` returned nothing).

**Conclusion (evidence-based):** The Android Kotlin/Java FL examples are **legacy and
unmaintained against current Flower**. They may still run via the compat layer, but this
was **not tested** in this session, and the README's stated JDK ceiling (≤16) conflicts
with this environment (JDK 21). Android FL participation must therefore be treated as a
**risk/blocker to be resolved in Phase 6** (Flower server + Python test clients), not as a
certainty.

### 2.3 On-device training (TFLite) — verified references

TFLite does support on-device training, but only for models exported with
`train`/`infer`/`save`/`restore` signatures and `experimental_enable_resource_variables`
at conversion time. Verified sources:

- Flower Kotlin example (read above): uses `Interpreter.runSignature(..., "train")`.
- Google AI Edge / LiteRT docs:
  https://developers.google.com/edge/litert/conversion/tensorflow/build/ondevice_training
  (verified via search result excerpts; the page describes `train`, `infer`, `save`,
  `restore` signatures and `converter.experimental_enable_resource_variables = True`).
- TensorFlow blog: https://blog.tensorflow.org/2021/11/on-device-training-in-tensorflow-lite.html

Implication: inference-only TFLite models (e.g. a plain classifier) **cannot** be
federated-trained on-device. The model must be built specifically for training from the
start (Phase 4/5 dependency).

---

## 3. Local environment reconnaissance (this machine)

Verified by running commands in this session:

| Item | Result |
|---|---|
| OS / shell | Linux, bash |
| Git | 2.43.0 |
- Git identity: `agni-007 <agniveshs53@gmail.com>` (global config present).
- No global `credential.helper` configured (push credential availability unknown).
- Python: `python3` = 3.12.3; `python3.11` available (3.11.16). Flower 1.40 requires
  Python >=3.11,<4.0 → use **python3.11** for a project virtualenv.
- Java: OpenJDK 21.0.12 (note: outside the Android Kotlin example's stated JDK ≤16).
- Installed Python ML packages: only `numpy 1.26.4` found. No `flwr`, no `tensorflow`,
  no `torch`.
- Android tooling: `adb` present at `/usr/bin/adb`. **Update:** an Android SDK **is**
  installed at `/home/agnivesh/Android/Sdk` (provided by the user after the initial
  probe). Verified contents: `build-tools/36.0.0` (+ Gradle later auto-installed
  `35.0.0`), `platforms/android-37.0`, `platform-tools`, `emulator`, `system-images`,
  and an accepted `licenses/android-sdk-license`. There is no `cmdline-tools`/`sdkmanager`
  and no standalone `gradle`; the project uses the Gradle wrapper instead.
  - Note: the platform directory is `android-37.0`, but AGP resolves
    `android-37`, so the project targets **compileSdk 36** (platform auto-installed by
    Gradle). See `docs/IMPLEMENTATION_PLAN.md` Phase 2.
- A Java/Gradle build was **verified successful** in Phase 2 (see `docs/SETUP.md`).
- **On-device verification (2026-10-16):** a real device (Realme RMX3868, Android 14) was
  connected over USB; `./gradlew installDebug` installed the debug APK, the app launched
  without crashing, and accelerometer capture streamed live with windowing active. See
  `docs/SETUP.md` → "Run on a real device" for commands and caveats.

---

## 2.4 Phase 6 verified findings (installed `flwr` 1.40.0)

Verified by running Python against the installed `.venv` in this session:

- `flwr.client.start_numpy_client` and `flwr.client.start_client` still exist but are
  **lazy, deprecated compatibility re-exports**:
  `.venv/.../flwr/client/__init__.py` `_LAZY_EXPORTS` maps them to
  `flwr.compat.client.app`. Both emit a deprecation warning at runtime.
- Both start functions are **keyword-only**: signature is
  `start_numpy_client(*, server_address, client, ...)`. Positional calls raise
  `TypeError`.
- The legacy Fleet gRPC transport used by these clients is started with
  `flwr.server.superlink.fleet.grpc_bidi.grpc_server.start_grpc_server(client_manager,
  server_address, ...)`.
- `flwr.server.server.init_defaults(server, config, strategy, client_manager)` returns
  `(Server, ServerConfig)` and builds a default `SimpleClientManager` when none is given.
- `History` stores distributed metrics as **dicts keyed by metric name**:
  `metrics_distributed_fit: dict[str, list[tuple[int, Scalar]]]` and
  `metrics_distributed: dict[str, list[tuple[int, Scalar]]]`. There is **no**
  `metrics_distributed_evaluate` attribute.
- `FedAvg.aggregate_fit` returns **empty fit metrics** unless a
  `fit_metrics_aggregation_fn` is supplied. We add an example-weighted
  `aggregate_metrics` function so aggregate accuracy/loss is recorded.
- Verified working: two clients registered against `start_grpc_server` and completed one
  FedAvg round in ~1.5 s (see `fl-server/tests/test_federated_round.py`). This confirms
  the legacy Python client/server path is functional under flwr 1.40.0 despite deprecation.

## 3. Reuse summary

| Component | Source | License | Use in FedRoad-Net |
|---|---|---|---|
| Accelerometer window shape (200×3) | ht-eml/pothole-detection | None (all rights reserved) | Concept reference only; do not copy code |
| Event/inference threshold idea | ht-eml/pothole-detection | None | Concept reference only |
| Precise-GPS upload path | ht-eml/pothole-detection | None | **Do NOT reuse** (privacy anti-pattern) |
| TFLite signature-based on-device training pattern | flwrlabs/flower `examples/android-kotlin` | Apache-2.0 (repo) | May adapt with attribution, if we choose TFLite |
| Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`) | flwrlabs/flower `examples/android-kotlin/client` (commit `f8b306c`) | Apache-2.0 (Gradle wrapper is standard) | Copied verbatim in Phase 2 into `android-app/`; `distributionUrl` updated to Gradle 8.13 |
| `FedAvgAndroid` + legacy `FlowerService` FL path | flwrlabs/flower | Apache-2.0 | Legacy/deprecated; verify in Phase 6 before use |

No upstream code has been copied into this repository in Phase 1.

---

## 4. Unresolved questions / risks

1. **Android FL client viability.** The only Android FL examples are legacy and target a
   deprecated protocol; local JDK (21) exceeds their tested range (≤16). The **Python**
   legacy client/server path was verified working in Phase 6 (section 2.4), but the
   **Android** client path remains untested. Decision still needed for Phase 7:
   (a) attempt the legacy Android path and verify, (b) keep Android inference-only and
   demonstrate FL with Python clients, or (c) use a third route.
2. **Android build environment.** An Android SDK **is** installed (see section 3), and the
   Android app builds successfully with the Gradle wrapper. A physical device/emulator
   run of sensor capture has **not** yet been performed, so on-device behavior (sensor
   rates, latency) is unverified.
3. **Dataset.** The pothole-detection dataset is not public and the repo has no data.
   Phase 4 needs a public, licensed accelerometer dataset or a clearly-labelled synthetic
   dataset. Model accuracy claims by the upstream author were not reproduced.
4. **On-device training model.** Requires a `train`-signature TFLite model; the upstream
   `frozen_model.pb` is not that format and is unlicensed.
5. **Push credentials.** No credential helper is configured; the first push may fail
   interactively. Reported honestly when attempted.
