# fl-server

Flower federated-learning server for FedRoad-Net.

**Status:** Phase 6. Implements the FedAvg strategy (with example-weighted metric
aggregation), tensor shape/dtype validation, and model-version validation. Includes
two Python test clients and an integration test that runs one real federated round
with two clients over the Flower Fleet gRPC transport.

## Setup

```sh
make install          # from the repo root; installs flwr==1.40.0 etc. into .venv
```

## Run

```sh
make fl-server                    # starts on 0.0.0.0:8080 (3 rounds)
python3.11 fl-server/test_client.py --id client1   # in another terminal
python3.11 fl-server/test_client.py --id client2   # in another terminal
```

## Test

```sh
make test
```

`tests/test_federated_round.py` starts a local Fleet gRPC server on an ephemeral port,
registers two clients, runs one FedAvg round, and asserts that round 1 completes with
aggregate-only metrics (no raw samples or coordinates).

## Verified APIs (flwr 1.40.0)

- `flwr.server.strategy.FedAvg` (with `fit_metrics_aggregation_fn`)
- `flwr.server.start_server`, `flwr.server.ServerConfig`
- `flwr.server.server.Server`, `init_defaults`
- `flwr.server.superlink.fleet.grpc_bidi.grpc_server.start_grpc_server`
- `flwr.client.start_numpy_client` (keyword-only: `server_address=`, `client=`)
- `flwr.common.ndarrays_to_parameters`, `parameters_to_ndarrays`

## Known limitations

- `start_server` / `start_numpy_client` are **deprecated** in Flower 1.40.0 (Flower
  points users to `flower-superlink` / `flower-supernode`). They remain functional and
  are used here for the legacy Fleet transport. Phase 7 (Android FL client) will assess
  whether the legacy protocol still works from Android.
- Ordinary FedAvg is **not** secure aggregation.

## Privacy note

This server only handles model parameters and training metadata. It must never receive
raw accelerometer windows or precise coordinates. Ordinary FedAvg is **not** secure
aggregation.
