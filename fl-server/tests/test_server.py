"""Phase 2 smoke tests for the federated server skeleton."""

from __future__ import annotations

import numpy as np
import pytest
from flwr.server.strategy import FedAvg

from server import (
    build_strategy,
    ndarrays_to_initial_parameters,
    validate_parameter_shapes,
)


def test_build_strategy_returns_fedavg_with_expected_min_clients() -> None:
    strategy = build_strategy(min_fit_clients=2, min_available_clients=2)
    assert isinstance(strategy, FedAvg)
    assert strategy.min_fit_clients == 2
    assert strategy.min_available_clients == 2


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
