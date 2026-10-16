"""FedRoad-Net coarse hazard reporting API (skeleton).

Phase 2 provides a health check and an empty aggregated-reports endpoint so the
environment and test tooling can be verified. The minimal coarse-report intake,
strict field validation, minimum-count aggregation, and privacy-boundary tests
are implemented in Phase 8.

Privacy design intent (NOT yet enforced in code):
  - Accept only coarse spatial cells, hazard class, a confidence band, and a
    coarse time bucket.
  - Reject precise coordinates, routes, and raw sensor payloads at the boundary.
  - Withhold cells below a minimum report count.
"""

from __future__ import annotations

from fastapi import FastAPI

app = FastAPI(
    title="FedRoad-Net Reporting API",
    version="0.1.0",
    description=(
        "Coarse, aggregated road-hazard reporting. Phase 2 skeleton only; "
        "input validation and minimum-count aggregation land in Phase 8."
    ),
)


@app.get("/health")
def health() -> dict[str, str]:
    """Liveness check."""
    return {"status": "ok"}


@app.get("/reports")
def list_reports() -> list[dict[str, object]]:
    """Return aggregated hazard cells.

    Phase 2 returns an empty list (no data source yet). The response is always
    aggregated cells only, never individual reports or precise locations.
    """
    return []
