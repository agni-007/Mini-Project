"""FedRoad-Net federated learning server (Phase 6).

Extends the Phase 2 skeleton with:
  - Schema validation: checks parameter shapes/dtypes/counts
  - Model versioning: validates a simple version string from clients
  - Simple smoke round support (uses FedAvg). Logs must not contain raw traces.

Privacy: server only handles model parameters and training metadata.
Ordinary FedAvg is NOT secure aggregation (documented limitation).
"""

from __future__ import annotations

from typing import Sequence

import numpy as np
from flwr.common import Parameters, ndarrays_to_parameters, parameters_to_ndarrays
from flwr.server.strategy import FedAvg

DEFAULT_SERVER_ADDRESS = "0.0.0.0:8080"
DEFAULT_NUM_ROUNDS = 3
DEFAULT_MODEL_VERSION = "v0.1.0-synthetic"


def fit_config(server_round: int) -> dict[str, int]:
    """Return per-round training configuration (no sensitive data)."""
    return {"batch_size": 32, "local_epochs": 1}


def aggregate_metrics(
    results: Sequence[tuple[int, dict[str, float | int]]],
) -> dict[str, float]:
    """Example-weighted mean of numeric client metrics.

    Returns only aggregate scalars. No raw sensor windows, coordinates, or
    per-client identifiers are produced by this function.
    """
    totals: dict[str, float] = {}
    total_examples = 0
    for num_examples, metrics in results:
        total_examples += num_examples
        for key, value in metrics.items():
            if isinstance(value, bool) or not isinstance(value, (int, float)):
                continue
            totals[key] = totals.get(key, 0.0) + float(value) * num_examples
    if total_examples <= 0:
        return {key: 0.0 for key in totals}
    return {key: value / total_examples for key, value in totals.items()}


def build_strategy(
    *,
    min_fit_clients: int = 2,
    min_available_clients: int = 2,
) -> FedAvg:
    """Build the default FedAvg strategy.

    Ordinary FedAvg is NOT secure aggregation; this is documented as a known
    limitation and is not claimed to be private.
    """
    return FedAvg(
        fraction_fit=1.0,
        fraction_evaluate=1.0,
        min_fit_clients=min_fit_clients,
        min_evaluate_clients=min_fit_clients,
        min_available_clients=min_available_clients,
        on_fit_config_fn=fit_config,
        fit_metrics_aggregation_fn=aggregate_metrics,
        evaluate_metrics_aggregation_fn=aggregate_metrics,
    )


def validate_parameter_shapes(
    parameters: Parameters,
    expected_shapes: Sequence[Sequence[int]],
    expected_dtypes: Sequence[np.dtype] | None = None,
) -> list[np.ndarray]:
    """Validate that *parameters* match *expected_shapes* and return arrays.

    Raises
    ------
    ValueError
        If the number of tensors, any tensor shape, or dtype does not match.
    """
    arrays = parameters_to_ndarrays(parameters)
    if len(arrays) != len(expected_shapes):
        raise ValueError(
            f"expected {len(expected_shapes)} tensors, got {len(arrays)}"
        )
    if expected_dtypes is not None and len(expected_dtypes) != len(expected_shapes):
        raise ValueError(
            f"expected {len(expected_shapes)} dtypes, got {len(expected_dtypes)}"
        )
    for index, (array, expected) in enumerate(zip(arrays, expected_shapes)):
        if tuple(array.shape) != tuple(expected):
            raise ValueError(
                f"tensor {index}: expected shape {tuple(expected)}, "
                f"got {tuple(array.shape)}"
            )
        if expected_dtypes is not None:
            expected_dtype = np.dtype(expected_dtypes[index])
            if array.dtype != expected_dtype:
                raise ValueError(
                    f"tensor {index}: expected dtype {expected_dtype.name}, "
                    f"got {array.dtype.name}"
                )
    return arrays


def validate_model_version(client_version: str, expected_version: str) -> None:
    """Validate a simple model version string from the client.

    Raises
    ------
    ValueError
        If versions do not match.
    """
    if client_version != expected_version:
        raise ValueError(
            f"model version mismatch: expected {expected_version}, got {client_version}"
        )


def ndarrays_to_initial_parameters(arrays: Sequence[np.ndarray]) -> Parameters:
    """Convert a list of numpy arrays into Flower ``Parameters``."""
    return ndarrays_to_parameters(list(arrays))


def main() -> None:
    """Start the Flower server (Phase 6)."""
    from flwr.server import ServerConfig, start_server

    strategy = build_strategy()
    start_server(
        server_address=DEFAULT_SERVER_ADDRESS,
        config=ServerConfig(num_rounds=DEFAULT_NUM_ROUNDS),
        strategy=strategy,
    )


if __name__ == "__main__":
    main()
