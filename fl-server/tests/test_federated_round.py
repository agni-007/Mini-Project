"""Phase 6 integration tests: real federated rounds with two Python clients.

Starts a Flower Fleet gRPC server, registers two local Python clients, runs a
single FedAvg round, then shuts everything down. Verifies:

  - both clients are selected and round 1 completes,
  - aggregated fit metrics are reported,
  - reported metrics are aggregate-only (loss/accuracy), with no raw sample
    payloads or precise coordinates.

Timing is made deterministic by waiting for client registration before calling
``Server.fit``.
"""

from __future__ import annotations

import socket
import threading
import time
from collections.abc import Iterator
from contextlib import contextmanager

from flwr.client import start_numpy_client
from flwr.server.client_manager import SimpleClientManager
from flwr.server.history import History
from flwr.server.server import Server, init_defaults
from flwr.server.server_config import ServerConfig
from flwr.server.superlink.fleet.grpc_bidi.grpc_server import start_grpc_server
from test_client import SimpleClient

from server import build_strategy

CLIENT_COUNT = 2
ROUND_TIMEOUT = 60.0
REGISTRATION_TIMEOUT = 30.0
METRIC_NAMES_FORBIDDEN = {
    "samples",
    "windows",
    "lat",
    "lon",
    "latitude",
    "longitude",
    "gps",
    "trace",
    "raw",
}


def _free_port() -> int:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
        sock.bind(("127.0.0.1", 0))
        return int(sock.getsockname()[1])


@contextmanager
def _fleet_server(
    num_rounds: int = 1,
) -> Iterator[tuple[Server, SimpleClientManager, str]]:
    """Start a Fleet gRPC server and yield (server, client_manager, address)."""
    strategy = build_strategy(
        min_fit_clients=CLIENT_COUNT,
        min_available_clients=CLIENT_COUNT,
    )
    client_manager = SimpleClientManager()
    server, config = init_defaults(
        server=None,
        config=ServerConfig(num_rounds=num_rounds),
        strategy=strategy,
        client_manager=client_manager,
    )
    address = f"127.0.0.1:{_free_port()}"
    grpc_server = start_grpc_server(
        client_manager=client_manager, server_address=address
    )
    try:
        yield server, client_manager, address
    finally:
        grpc_server.stop(grace=1.0)


def _wait_for_clients(
    client_manager: SimpleClientManager, count: int, errors: list[BaseException]
) -> None:
    deadline = time.monotonic() + REGISTRATION_TIMEOUT
    while client_manager.num_available() < count:
        if errors:
            raise AssertionError(f"client failed to start: {errors[0]}") from errors[0]
        if time.monotonic() > deadline:
            raise TimeoutError(
                f"only {client_manager.num_available()} of {count} clients "
                "registered before deadline"
            )
        time.sleep(0.05)


def _run_round() -> History:
    """Run one FedAvg round with two locally registered clients."""
    errors: list[BaseException] = []

    def _client_main(client_id: str, address: str) -> None:
        try:
            start_numpy_client(
                server_address=address,
                client=SimpleClient(client_id),
            )
        except BaseException as exc:  # noqa: BLE001 - surfaced in assertions
            errors.append(exc)

    with _fleet_server(num_rounds=1) as (server, client_manager, address):
        threads = [
            threading.Thread(
                target=_client_main, args=(f"client-{i}", address), daemon=True
            )
            for i in range(1, CLIENT_COUNT + 1)
        ]
        for thread in threads:
            thread.start()

        _wait_for_clients(client_manager, count=CLIENT_COUNT, errors=errors)
        assert client_manager.num_available() == CLIENT_COUNT

        history, _elapsed = server.fit(num_rounds=1, timeout=ROUND_TIMEOUT)
        server.disconnect_all_clients(timeout=5.0)

        for thread in threads:
            thread.join(timeout=15.0)

        hung = [t.name for t in threads if t.is_alive()]
        assert not errors, f"client raised: {errors}"
        assert not hung, f"client thread(s) hung: {hung}"

    return history


def test_two_clients_complete_one_federated_round() -> None:
    history = _run_round()

    fit_metrics = history.metrics_distributed_fit
    assert fit_metrics, "no aggregated fit metrics recorded"
    assert "accuracy" in fit_metrics
    assert "loss" in fit_metrics

    accuracy_by_round = dict(fit_metrics["accuracy"])
    assert 1 in accuracy_by_round
    assert isinstance(accuracy_by_round[1], (int, float))


def test_reported_metrics_are_aggregate_only() -> None:
    """Aggregate metrics must not carry raw samples or precise coordinates."""
    history = _run_round()

    metric_names = set(history.metrics_distributed_fit) | set(
        history.metrics_distributed
    )
    leaked = METRIC_NAMES_FORBIDDEN.intersection(name.lower() for name in metric_names)
    assert not leaked, f"reported metric names leaked: {leaked}"
