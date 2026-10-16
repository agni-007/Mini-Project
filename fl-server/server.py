"""FedRoad-Net federated learning server (skeleton).

This is a minimal, dependency-verified skeleton for Phase 2. The full federated
round, schema validation, and tests are implemented in Phase 6.

Verified against flwr 1.40.0:
  - ``flwr.server.strategy.FedAvg`` exists.
  - ``flwr.server.start_server`` and ``flwr.server.ServerConfig`` exist.
  - ``flwr.common.parameters_to_ndarrays`` exists.

Privacy note: this server only handles model parameters and training metadata.
It must never receive raw accelerometer windows or precise coordinates.
"""

from __future__ import annotations

from typing import Sequence

import numpy as np
from flwr.common import Parameters, ndarrays_to_parameters, parameters_to_ndarrays
from flwr.server.strategy import FedAvg

DEFAULT_SERVER_ADDRESS = "0.0.0.0:8080"
DEFAULT_NUM_ROUNDS = 3


def fit_config(server_round: int) -> dict[str, int]:
    """Return per-round training configuration (no sensitive data)."""
    return {"batch_size": 32, "local_epochs": 1}


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
    )


def validate_parameter_shapes(
    parameters: Parameters,
    expected_shapes: Sequence[Sequence[int]],
) -> list[np.ndarray]:
    """Validate that *parameters* match *expected_shapes* and return arrays.

    Raises
    ------
    ValueError
        If the number of tensors or any tensor shape does not match.
    """
    arrays = parameters_to_ndarrays(parameters)
    if len(arrays) != len(expected_shapes):
        raise ValueError(
            f"expected {len(expected_shapes)} tensors, got {len(arrays)}"
        )
    for index, (array, expected) in enumerate(zip(arrays, expected_shapes)):
        if tuple(array.shape) != tuple(expected):
            raise ValueError(
                f"tensor {index}: expected shape {tuple(expected)}, "
                f"got {tuple(array.shape)}"
            )
    return arrays


def ndarrays_to_initial_parameters(arrays: Sequence[np.ndarray]) -> Parameters:
    """Convert a list of numpy arrays into Flower ``Parameters``."""
    return ndarrays_to_parameters(list(arrays))


def main() -> None:
    """Start the Flower server (Phase 2 smoke path; extended in Phase 6)."""
    from flwr.server import ServerConfig, start_server

    strategy = build_strategy()
    start_server(
        server_address=DEFAULT_SERVER_ADDRESS,
        config=ServerConfig(num_rounds=DEFAULT_NUM_ROUNDS),
        strategy=strategy,
    )


if __name__ == "__main__":
    main()
