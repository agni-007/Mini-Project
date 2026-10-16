# fl-server

Flower federated-learning server for FedRoad-Net.

**Status:** Phase 2 skeleton. Implements a minimal `FedAvg` strategy builder and a
parameter-shape validator. The full federated round, schema validation, dropout/timeout
handling, and tests are implemented in Phase 6.

## Setup

```sh
make install          # from the repo root; installs flwr==1.40.0 etc. into .venv
```

## Run

```sh
make fl-server        # starts on 0.0.0.0:8080 (3 rounds)
```

## Test

```sh
make test
```

## Verified APIs (flwr 1.40.0)

- `flwr.server.strategy.FedAvg`
- `flwr.server.start_server`, `flwr.server.ServerConfig`
- `flwr.common.ndarrays_to_parameters`, `parameters_to_ndarrays`

## Privacy note

This server only handles model parameters and training metadata. It must never receive
raw accelerometer windows or precise coordinates. Ordinary FedAvg is **not** secure
aggregation.
