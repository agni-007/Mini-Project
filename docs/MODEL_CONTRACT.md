# FedRoad-Net — Model Contract (Phase 4 template)

> **Status:** TEMPLATE ONLY. Do not fabricate dataset size, accuracy, labels, or
> normalisation constants. These must be filled in once a concrete, clearly
> licensed dataset (or approved synthetic dataset) is selected and inspected.

## 1. Sensor and capture
- **Sensor:** Android `Sensor.TYPE_ACCELEROMETER` (includes gravity)
- **Units:** m/s²
- **Requested sampling rate:** `50 Hz` (configurable via `WindowConfig.sampleRateHz`)
- **Window length:** `128 samples` per axis (default) — **TBD** based on dataset
- **Axes order:** `[x, y, z]`
- **Timestamping:** sensor event timestamp in nanoseconds (kept on-device only)

## 2. Windowing and preprocessing
- **Buffering:** bounded to window size; oldest samples dropped when full
- **Trigger:** gravity-removed linear acceleration magnitude > `2.5 m/s²` (default)
- **Cooldown:** `64 samples` (default) to avoid duplicate windows
- **Gravity estimation:** low-pass exponential smoothing with `alpha = 0.8`
- **Preprocessing:** axis-interleaved layout `[..., x_i, y_i, z_i, ...]`, oldest first
- **Gravity removal:** `true` by default (linear acceleration)
- **Scale:** `1.0` by default — **TBD** (must match training)
- **Mean/std normalisation:** **TBD** or `none`
- **Input tensor shape (expected):** `(batch, window_size, 3)` or flattened `(batch, 3*window_size)` — **TBD**
- **Input dtype:** `float32`

## 3. Labels and classes
- **Label set:** **TBD**
- **Class balance:** **TBD** (not reported)
- **Train/val/test split:** **TBD**

## 4. Model
- **Architecture:** lightweight baseline (e.g. 1D CNN/LSTM) — **TBD** only if data allows
- **Output:** class probabilities, shape `(batch, num_classes)` — **TBD**
- **Model format for mobile:** TensorFlow Lite with `train`/`infer`/`save`/`restore` signatures **if** on-device training is required; otherwise standard `.tflite` — **TBD**

## 5. Federated learning contract
- **Strategy:** `FedAvg` (ordinary, **not** secure aggregation) — known limitation
- **Parameter ordering/shapes:** **TBD** after model definition
- **Client metadata:** minimal training metadata only (no raw traces/precise locations)
- **Versioning:** `model_version` **TBD**

## 6. Dataset (blocker)
- **Source:** **TBD** — must be publicly available with a license compatible with this project (Apache-2.0), or an approved synthetic dataset clearly labelled as demo
- **Curation notes:** **TBD**
- **Attribution:** **TBD**

**No accuracy numbers, dataset size, or benchmark results are claimed.** Phase 4 will remain blocked until a concrete dataset is selected and inspected.
