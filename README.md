# FedRoad-Net

A privacy-preserving federated road-hazard and pothole mapping prototype using mobile
edge devices. Android phones capture accelerometer motion, classify possible road events
on-device, optionally contribute model updates to a Flower federated server, and
optionally upload only coarse, aggregated hazard reports for a municipal dashboard.

> **Project status: Phase 1 (reconnaissance) complete.** This README describes the
> project intent and verified constraints. No application code has been written yet.
> Claims are labelled **verified**, **planned**, or **blocked**. See
> `docs/REFERENCE_NOTES.md` and `docs/IMPLEMENTATION_PLAN.md`.

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

## Repository layout (planned)

```text
docs/            # reference notes, plan, model contract, privacy/data flow
ml/              # dataset handling, training, evaluation, conversion
fl-server/       # Flower server and Python test clients
reporting-api/   # coarse hazard-report API with validation + aggregation
dashboard/       # Leaflet + OpenStreetMap aggregated view
android-app/     # Android capture + inference (placeholder until Phase 3/5)
implementation (1).md   # original requirements / phase plan (source of truth)
```

## Phase status

| Phase | Description | Status |
|---|---|---|
| 1 | Reconnaissance + plan | Complete |
| 2 | Skeleton, env, quality checks | Pending approval |
| 3 | Sensor capture + windowing | Pending |
| 4 | Dataset + baseline model | Blocked (dataset choice) |
| 5 | Mobile conversion + local inference | Pending |
| 6 | Flower server + Python test clients | Pending |
| 7 | Android FL integration | Risk (legacy protocol) |
| 8 | Coarse reporting API | Pending |
| 9 | Hotspot dashboard | Pending |
| 10 | End-to-end validation + docs | Pending |

## Verified environment constraints (Phase 1)

- Target remote: `https://github.com/agni-007/Mini-Project.git` (was empty).
- Python 3.11 available; Flower requires `>=3.11,<4.0` (latest release 1.40.0).
- No Android SDK is installed locally; no `flwr`/`tensorflow`/`torch` installed yet.
- Reference `ht-eml/pothole-detection` has **no license** → concept reference only.

## References

- Flower (Apache-2.0): https://github.com/flwrlabs/flower
- Pothole detection (no license; reference only):
  https://github.com/ht-eml/pothole-detection

See `docs/REFERENCE_NOTES.md` for exact commits, file paths, and verified API details.
