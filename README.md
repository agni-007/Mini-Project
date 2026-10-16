# FedRoad-Net

A privacy-preserving federated road-hazard and pothole mapping prototype using mobile
edge devices. Android phones capture accelerometer motion, classify possible road events
on-device, optionally contribute model updates to a Flower federated server, and
optionally upload only coarse, aggregated hazard reports for a municipal dashboard.

> **Project status: Phase 6 (Flower server + Python test clients) complete.** The
> repository has a buildable Android app, an on-device inference scaffold, a Flower server
> with validation + metrics aggregation, and a verified two-client federated round. See
> `docs/SETUP.md`, `docs/REFERENCE_NOTES.md`, and `docs/IMPLEMENTATION_PLAN.md`.

## Goals

1. **Data minimization** — raw accelerometer windows and precise GPS traces stay
   on-device by default.
2. **On-device inference** — a lightweight model classifies possible road events
   locally.
3. **Federated learning** — participating devices train locally and contribute model
   updates to a Flower server.
4. **Useful hotspot mapping** — only coarse, aggregated hazard reports are displayed.

## Privacy scope (important, and not over-stated)

- Federated model updates **alone cannot produce a map**. Hotspot mapping needs a
  separate, explicitly designed coarse-reporting path.
- Model updates are **not inherently private**; ordinary FedAvg is **not** secure
  aggregation, and TLS is **not** the same as secure aggregation.
- Raw accelerometer windows and precise coordinates are kept on-device; the reporting
  API is intended to reject precise coordinates and raw sensor payloads.
- No production security or anonymity guarantees are claimed.

## Repository layout

```text
docs/            # reference notes, plan, setup, model contract, privacy/data flow
ml/              # dataset handling, training, evaluation, conversion (Phase 4+)
fl-server/       # Flower server skeleton + tests
reporting-api/   # coarse hazard-report API skeleton + tests
dashboard/       # Leaflet + OpenStreetMap aggregated view (Phase 9)
android-app/     # Kotlin Android app (buildable skeleton; sensors in Phase 3)
Makefile         # install / lint / test / run targets
pytest.ini       # pytest config
ruff.toml        # lint config
implementation (1).md   # original requirements / phase plan (source of truth)
```

## Phase status

| Phase | Description | Status |
|---|---|---|
| 1 | Reconnaissance + plan | Complete |
| 2 | Skeleton, env, quality checks | Complete |
| 3 | Sensor capture + windowing | Complete |
| 4 | Dataset + baseline model | Scaffolded (BLOCKED on dataset) |
| 5 | Mobile conversion + local inference | Scaffolded (no model yet) |
| 6 | Flower server + Python test clients | Complete |
| 7 | Android FL integration | Risk (legacy protocol) |
| 8 | Coarse reporting API | Pending |
| 9 | Hotspot dashboard | Pending |
| 10 | End-to-end validation + docs | Pending |

## Getting started

```sh
make install         # python3.11 venv + pinned deps
make lint            # ruff
make test            # pytest (14 tests: FL round, server validation, app, ml)
make android-build   # ./gradlew assembleDebug (needs Android SDK)
```

Full details: `docs/SETUP.md`.

## Verified environment constraints (Phase 1)

- Target remote: `https://github.com/agni-007/Mini-Project.git` (was empty).
- Python 3.11 available; Flower requires `>=3.11,<4.0` (latest release 1.40.0).
- Android SDK present at `/home/agnivesh/Android/Sdk`; the Android app builds with the
  Gradle wrapper (compileSdk 36). No physical device/emulator run yet.
- Reference `ht-eml/pothole-detection` has **no license** → concept reference only.

## References

- Flower (Apache-2.0): https://github.com/flwrlabs/flower
- Pothole detection (no license; reference only):
  https://github.com/ht-eml/pothole-detection

See `docs/REFERENCE_NOTES.md` for exact commits, file paths, and verified API details.
