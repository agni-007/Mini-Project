"""Phase 2 smoke tests for the reporting API skeleton."""

from __future__ import annotations

from fastapi.testclient import TestClient

from app import app

client = TestClient(app)


def test_health_ok() -> None:
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_reports_returns_aggregated_list() -> None:
    response = client.get("/reports")
    assert response.status_code == 200
    assert response.json() == []
