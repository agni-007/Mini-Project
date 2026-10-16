#!/usr/bin/env python3
"""Simple Flower test client (Python) for Phase 6.

This client demonstrates a minimal FL round using the Flower API. It:
  - connects to the server (default localhost:8080),
  - returns trivial parameters (zeros) when asked,
  - simulates local training/evaluation,
  - reports minimal training metadata.

This is for demonstration only; it uses randomly-initialised-like zeros and
does not train a real model. No raw sensor data or precise coordinates are
transmitted.

Run:
  python3.11 fl-server/test_client.py --id 1
"""

from __future__ import annotations

import argparse

import flwr as fl
import numpy as np


class SimpleClient(fl.client.NumPyClient):
    def __init__(self, client_id: str) -> None:
        self.client_id = client_id
        # Simple model: 2 tensors matching a common small shape
        self.model_parameters = [
            np.zeros((4, 3), dtype=np.float32),
            np.zeros((2,), dtype=np.float32),
        ]
        self.model_version = "v0.1.0-synthetic"

    def get_parameters(self, config: dict) -> list[np.ndarray]:
        return [p.copy() for p in self.model_parameters]

    def fit(
        self, parameters: list[np.ndarray], config: dict
    ) -> tuple[list[np.ndarray], int, dict]:
        # "Train" by doing a no-op (simulated). In a real client this would
        # perform local training on on-device data.
        self.model_parameters = [p.copy() for p in parameters]
        num_examples = 10
        metrics: dict[str, float] = {
            "loss": 0.5,
            "accuracy": 0.6,
        }
        return [p.copy() for p in self.model_parameters], num_examples, metrics

    def evaluate(
        self, parameters: list[np.ndarray], config: dict
    ) -> tuple[float, int, dict]:
        self.model_parameters = [p.copy() for p in parameters]
        loss = 0.4
        num_examples = 5
        metrics = {"accuracy": 0.65}
        return loss, num_examples, metrics


def start_client(server_address: str, client_id: str) -> None:
    client = SimpleClient(client_id)
    fl.client.start_numpy_client(server_address=server_address, client=client)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Simple Flower test client")
    parser.add_argument("--address", type=str, default="127.0.0.1:8080")
    parser.add_argument("--id", type=str, default="client1")
    return parser.parse_args()


if __name__ == "__main__":
    args = parse_args()
    start_client(f"{args.address}", args.id)
