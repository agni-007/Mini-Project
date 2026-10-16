"""Phase 6 tests for server schema validation and model version."""

from __future__ import annotations

import numpy as np
import pytest
from flwr.server.strategy import FedAvg

from server import (
    aggregate_metrics,
    build_strategy,
    ndarrays_to_initial_parameters,
    validate_model_version,
    validate_parameter_shapes,
)


def test_build_strategy_returns_fedavg_with_expected_min_clients() -> None:
    strategy = build_strategy(min_fit_clients=2, min_available_clients=2)
    assert isinstance(strategy, FedAvg)
    assert strategy.min_fit_clients == 2
    assert strategy.min_available_clients == 2


def test_build_strategy_wires_metric_aggregation() -> None:
    strategy = build_strategy()
    assert strategy.fit_metrics_aggregation_fn is not None
    assert strategy.evaluate_metrics_aggregation_fn is not None


def test_aggregate_metrics_is_example_weighted_mean() -> None:
    results = [
        (10, {"accuracy": 0.2, "loss": 1.0}),
        (30, {"accuracy": 0.6, "loss": 2.0}),
    ]
    aggregated = aggregate_metrics(results)
    assert aggregated["accuracy"] == pytest.approx((0.2 * 10 + 0.6 * 30) / 40)
    assert aggregated["loss"] == pytest.approx((1.0 * 10 + 2.0 * 30) / 40)


def test_aggregate_metrics_ignores_non_numeric_entries() -> None:
    results = [(4, {"accuracy": 0.5, "note": "raw", "flag": True})]
    aggregated = aggregate_metrics(results)
    assert aggregated == {"accuracy": 0.5}


def test_validate_parameter_shapes_accepts_matching_shapes() -> None:
    arrays = [np.zeros((4, 3), dtype=np.float32), np.zeros((2,), dtype=np.float32)]
    parameters = ndarrays_to_initial_parameters(arrays)
    result = validate_parameter_shapes(parameters, [(4, 3), (2,)])
    assert [tuple(a.shape) for a in result] == [(4, 3), (2,)]


def test_validate_parameter_shapes_rejects_wrong_tensor_count() -> None:
    arrays = [np.zeros((4,), dtype=np.float32)]
    parameters = ndarrays_to_initial_parameters(arrays)
    with pytest.raises(ValueError, match="expected 2 tensors"):
        validate_parameter_shapes(parameters, [(4,), (2,)])


def test_validate_parameter_shapes_rejects_wrong_shape() -> None:
    arrays = [np.zeros((4,), dtype=np.float32), np.zeros((2,), dtype=np.float32)]
    parameters = ndarrays_to_initial_parameters(arrays)
    with pytest.raises(ValueError, match="tensor 0"):
        validate_parameter_shapes(parameters, [(5,), (2,)])


def test_validate_parameter_shapes_validates_dtypes() -> None:
    arrays = [np.zeros((4,), dtype=np.float32)]
    parameters = ndarrays_to_initial_parameters(arrays)
    result = validate_parameter_shapes(parameters, [(4,)], [np.float32])
    assert result[0].dtype == np.float32
    with pytest.raises(ValueError, match="dtype"):
        validate_parameter_shapes(parameters, [(4,)], [np.float64])


def test_validate_model_version_accepts_matching() -> None:
    validate_model_version("v0.1.0", "v0.1.0")


def test_validate_model_version_rejects_mismatch() -> None:
    with pytest.raises(ValueError, match="mismatch"):
        validate_model_version("v0.1.0", "v0.2.0")
