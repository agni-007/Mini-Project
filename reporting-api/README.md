# reporting-api

Coarse, aggregated road-hazard reporting API for FedRoad-Net.

**Status:** Phase 2 skeleton. Provides `/health` and an empty `/reports` endpoint.
Strict coarse-report intake, prohibited-field rejection, and minimum-count aggregation
are implemented in Phase 8.

## Setup / Run / Test

```sh
make install          # from repo root
make reporting-api    # uvicorn on 127.0.0.1:8000
make test
```

## Endpoints (Phase 2)

- `GET /health` → `{"status": "ok"}`
- `GET /reports` → `[]` (aggregated cells only; no data source yet)

## Privacy design intent (enforced from Phase 8)

- Accept only coarse spatial cells, hazard class, confidence band, time bucket.
- Reject precise coordinates, routes, and raw sensor payloads at the boundary.
- Withhold cells below a minimum report count.
- Keep demo/synthetic reports clearly separated and labelled.
