# ml

Host-side machine learning: dataset handling, baseline training, evaluation, and mobile
model conversion.

**Status:** Phase 4 (scaffolded). Dataset choice is currently **BLOCKED** (reference
`ht-eml/pothole-detection` has no public dataset and no license). A synthetic,
clearly-labelled demo dataset may be used later for pipeline validation only; no
real-world accuracy is claimed.

Planned contents (when unblocked):
- `data/` — local dataset (gitignored; never commit personal or licensed-restricted data)
- `train.py`, `evaluate.py`, `convert_model.py`
- `requirements.txt` — pinned host training dependencies (added when needed)

See `docs/MODEL_CONTRACT.md` for the contract template (to be filled in once data is
inspected). Do not add accuracy, dataset size, or labels until verified.
