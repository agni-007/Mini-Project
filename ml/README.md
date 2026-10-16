# ml

Host-side machine learning: dataset handling, baseline training, evaluation, and mobile
model conversion.

**Status:** Phase 4+. No code yet, because the labelled dataset choice is unresolved
(the reference pothole repo has no public dataset and no license). See
`docs/IMPLEMENTATION_PLAN.md`.

Planned contents:

- `data/` — local dataset (gitignored; never commit personal or licensed-restricted data)
- `train.py`, `evaluate.py`, `convert_model.py`
- `requirements.txt` — pinned host training dependencies (added in Phase 4)

Do not add model accuracy, dataset size, or label claims here until they are actually
produced and verified.
