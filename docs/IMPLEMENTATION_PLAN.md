# FedRoad-Net — Implementation Plan (Phase 1 draft)

Status legend: **VERIFIED** (inspected/run), **PLANNED** (not yet built),
**BLOCKED** (needs input/hardware/credentials), **RISK** (may fail).

This plan conforms to the ten-phase plan and privacy rules in `implementation (1).md`.
It is deliberately minimal and will be revised after each phase.

---

## 1. Verified facts carried into the plan

From `docs/REFERENCE_NOTES.md`:

- Target repo is empty; workspace was initialised fresh with `origin` =
  `https://github.com/agni-007/Mini-Project.git`. **VERIFIED**
- `ht-eml/pothole-detection` has **no license** → concept reference only. **VERIFIED**
- Flower 1.41.0 in-tree; latest PyPI release `flwr 1.40.0`; requires Python >=3.11,<4.0;
  Apache-2.0. **VERIFIED**
- Flower Android Kotlin/Java examples still exist but use the **deprecated
  `FedAvgAndroid` + legacy `FlowerService`** protocol; README tested JDK ≤16; no CI job.
  **VERIFIED (as legacy) / RISK**
- TFLite supports on-device training only via `train`/`infer`/`save`/`restore`
  signatures. **VERIFIED**
- Local environment: python3.11 present, JDK 21, **no Android SDK**, no flwr/tensorflow
  installed. **VERIFIED**

---

## 2. Target architecture (Phase 1 proposal)

```
Android phone
  ├── Consent + collection controls                (Phase 2/8 UI)
  ├── Accelerometer capture + bounded window buffer (Phase 3)
  ├── Preprocessing + local TFLite inference        (Phase 5)
  ├── Optional local event store                    (Phase 3)
  ├── FL client adapter                             (Phase 6, RISK)
  └── Optional coarse hazard report  ──────────────┐
                                                    │
        FL transport (legacy gRPC, if verified)     │
                 ▼                                  ▼
   Python Flower server (FedAvgAndroid)     Reporting API (FastAPI/Flask)
                                                    │  validate + aggregate
                                                    ▼
                                            Leaflet + OSM dashboard
```

Technology choices (to be reconsidered in later phases):

| Layer | Choice | Status |
|---|---|---|
| Android | Kotlin, Gradle, Android SDK | BLOCKED locally (no SDK) |
| Sensor | Android `SensorManager` | PLANNED |
| On-device runtime | TFLite/LiteRT with `train`/`infer` signatures | PLANNED |
| Host training | Python + TensorFlow/Keras | PLANNED |
| FL orchestration | `flwr` pinned to a released version (e.g. 1.40.0) | PLANNED |
| Reporting API | Python (FastAPI or Flask — decide in Phase 8) | PLANNED |
| Dashboard | Leaflet + OpenStreetMap | PLANNED |

---

## 2.1 Key architectural decision to confirm with the user

Because the Android FL examples are legacy/deprecated and the local JDK is out of their
tested range, two options exist:

- **Option A (recommended, lower risk):** Build the FL demonstration with a Flower
  server + **two Python test clients** first (Phase 6), and treat the Android app as the
  data-collection + on-device-inference contributer. Attempt Android FL participation only
  after the Python path works, and only if the legacy protocol is confirmed functional.
- **Option B:** Try to revive the legacy `FedAvgAndroid` + `FlowerService` Android client
  directly. Higher risk; depends on an untested, deprecated path and a JDK downgrade.

The plan below is written for **Option A** unless the user chooses otherwise. This does
not remove any required feature; it sequences the risky part later and keeps the
acceptance criteria achievable.

---

## 3. Phase-by-phase plan

### Phase 1 — Reconnaissance (COMPLETE)
Deliver `docs/REFERENCE_NOTES.md`, this plan, `README.md`, fresh Git repo + remote.

### Phase 2 — Project skeleton and quality checks (PLANNED)
- Create `ml/`, `fl-server/`, `reporting-api/`, `dashboard/`, `android-app/` (placeholder).
- Add `.gitignore` (venv, build outputs, `*.keystore`, real data, model binaries).
- Pin Python deps (`flwr==1.40.0`, `numpy`, test tooling) in `fl-server/pyproject.toml`
  or `requirements.txt`. Use `python3.11` virtualenv.
- Add lint/test entry points; create empty-state tests that actually run.
- **Exit:** fresh checkout + documented commands run cleanly.

### Phase 3 — Sensor capture + windowing (PLANNED)
- Kotlin accelerometer capture with bounded ring buffer, configurable sample rate, and
  event trigger; fixed-length window extraction.
- Unit tests for windowing/preprocessing on the JVM (no device required where possible).
- **Blocker:** Android SDK needed to compile. May document + unit-test pure Kotlin logic.
- **Exit:** tested windowing logic; no raw data leaves device.

### Phase 4 — Dataset + baseline model (BLOCKED on data)
- pothole-detection dataset is NOT public and repo has no data.
- Need either a public open-licensed accelerometer road dataset or a clearly labelled
  synthetic set. **User decision required.**
- Train/evaluate baseline in Python; write `docs/MODEL_CONTRACT.md` with real shapes/labels.
- **Exit:** reproducible baseline + honest evaluation, or documented blocker.

### Phase 5 — Mobile model conversion + inference (PLANNED)
- Convert baseline to TFLite; if FL training is required, export `train`/`infer`/
  `save`/`restore` signatures so on-device training is technically possible.
- Verify numerical consistency host vs TFLite (Python interpreter at minimum).
- **Blocker:** on-device latency measurement needs a physical device.

### Phase 6 — Flower server + Python test clients (PLANNED)
- Pin `flwr`; implement server strategy + schema/parameter validation.
- Two Python clients complete a full federated round; test dropout/timeout/invalid params.
- **Exit:** reproducible FL round with logs containing no raw traces.

### Phase 7 — Android FL integration (RISK / may be BLOCKED)
- Only attempt after Phase 6. Verify legacy protocol end-to-end. If unsupported, document
  evidence and keep Android as inference-only (per Option A). No fabricated client.

### Phase 8 — Reporting API (PLANNED)
- FastAPI/Flask endpoint accepting only `(coarse_cell_id, hazard_class, confidence_band,
  time_bucket)`; reject precise coords and raw sensor payloads with tests.
- Minimum-count aggregation; separate demo vs real data.

### Phase 8.5 / 9 — Leaflet dashboard (PLANNED)
- Aggregated cells only, legend, freshness, empty state, demo-data indicator.

### Phase 10 — End-to-end validation + docs (PLANNED)
- Run all tests, clean-start procedure, honest README, secret/diff review.

---

## 4. Privacy enforcement points

- On-device only: raw accelerometer windows, precise GPS.
- Reporting API boundary rejects: exact lat/lon high precision, raw arrays, routes,
  extra fields.
- Dashboard shows only cells meeting `min_count`, with demo/real separation labels.
- README must state FedAvg ≠ secure aggregation and TLS ≠ privacy.

---

## 4.1 Open questions for the user

1. Approve **Option A** for Phase 6/7 (Python FL clients first; Android FL only if the
   legacy path is verified)?
2. Can you provide/approve an Android SDK install path, or should Android builds stay
   documented-but-unverified until a machine with Android Studio is available?
3. Which dataset route for Phase 4: public open dataset, synthetic demo data, or a
   dataset you will supply?
