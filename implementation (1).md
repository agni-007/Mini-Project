# FedRoad-Net — SRS, Implementation Blueprint, and AI Agent Execution Guide

> **Project:** FedRoad-Net — A Privacy-Preserving Federated Road Hazard and Pothole Mapping Network Using Mobile Edge Devices  
> **Purpose of this document:** Give an autonomous coding agent a grounded, phased implementation plan. The agent must inspect the actual repositories and project state before coding, work in small reviewable changes, and stop after every phase for human review.

---

## 1. Project Overview

FedRoad-Net uses Android smartphones as edge devices to identify possible road hazards from motion-sensor data. A lightweight model performs inference on the phone. Participating devices can contribute to a shared model through federated learning (FL), coordinated by a Python server using Flower. A web dashboard can display aggregated hazard hotspots for maintenance teams.

### Goals

1. **Data minimization:** Keep raw accelerometer windows and precise GPS traces on the phone by default.
2. **On-device inference:** Run a lightweight model on Android to classify possible road events such as potholes, severe bumps, and speed breakers.
3. **Federated learning:** Allow participating devices to train locally and contribute model updates to a Flower server.
4. **Useful hotspot mapping:** Display only coarse, aggregated hazard reports—not individual users' routes or precise location histories.
5. **Practical student-project scope:** Build a testable end-to-end prototype before attempting production-grade security, scale, or deployment.

### Important scope clarification

Federated model updates alone cannot create a map of where a pothole is located. The dashboard therefore needs a separate, explicitly designed hazard-reporting path. If hotspot mapping is enabled, the phone should convert a detected event into a **coarse spatial cell** locally and upload only a minimal report such as `(coarse_cell_id, hazard_class, confidence_band, time_bucket)`. Do not upload raw GPS coordinates, a route, or a timestamp precise enough to reconstruct a user's movement. Apply minimum-count thresholds before showing cells on the dashboard, and document the residual privacy risks.

Likewise, **TLS and federated learning do not automatically guarantee privacy**. Model updates can leak information, and ordinary FedAvg is not secure aggregation. Do not describe the prototype as perfectly anonymous or fully secure. State exactly what data is sent, implement transport security for deployment, and record known limitations.

---

## 2. System Requirements Specification (SRS)

### 2.1 Functional requirements

**FR-01 — Sensor capture**
- Read Android accelerometer samples using the Android sensor APIs.
- Use a configurable sampling rate and a bounded in-memory buffer.
- Detect candidate events using a configurable threshold or another explainable trigger.
- Capture a fixed-length time window around a candidate event where feasible.
- Handle sensor unavailability, app lifecycle changes, and device interruptions gracefully.

**FR-02 — On-device inference**
- Load a model from the app's packaged assets or another documented local model location.
- Validate the model input shape, data type, normalization, output labels, and version.
- Run inference locally and show the result in a basic debug/status UI.
- Do not assume a model exists or is compatible until the repository and model artifacts have been inspected.

**FR-03 — Local event storage**
- Store only the minimum data needed for testing and optional local training.
- Keep raw sensor windows on-device by default.
- Provide a clear retention/deletion policy and avoid collecting data before the user has been informed.
- Use synthetic or openly licensed sample data for initial development where possible.

**FR-04 — Federated client**
- Connect a client to a compatible Flower server using a verified, supported protocol.
- Receive global parameters, perform local training if the chosen Android runtime supports it, and return model parameters plus the required training metadata.
- Handle offline devices, timeouts, retries, version mismatch, and incomplete rounds.
- Never silently substitute a made-up API or invented Flower Android interface.

**FR-05 — Federated server**
- Run a Python Flower server with a documented strategy, initially FedAvg if appropriate.
- Configure minimum available/fit clients and round timeouts for local testing.
- Validate model parameter count, tensor shapes, dtypes, and client metadata before aggregation.
- Log operational metrics without logging raw sensor windows, precise coordinates, or personal identifiers.

**FR-06 — Aggregated hazard reports**
- Keep this path separate from model-weight exchange.
- If mapping is enabled, accept only coarse cell IDs and minimal event metadata.
- Reject precise coordinates and unexpected fields at the API boundary.
- Aggregate reports and enforce a configurable minimum report count before a cell is shown.
- Clearly label demo/test reports and do not present simulated reports as real observations.

**FR-07 — Dashboard**
- Show aggregated hazard cells and hazard categories on a map using Leaflet and OpenStreetMap or an equivalent documented choice.
- Include a legend, data freshness indicator, and a clear explanation of aggregation.
- Do not expose individual devices, paths, precise coordinates, or raw sensor records.
- Provide an empty state when there is insufficient data to display a hotspot.

**FR-08 — User control and transparency**
- Explain sensor collection, local processing, network transmissions, and limitations in plain language.
- Ask for appropriate permissions and consent before enabling relevant features.
- Provide controls to pause collection and delete locally stored events.
- Do not claim Android can run continuous background sensing indefinitely; follow current Android background execution and permission requirements.

**FR-09 — Testing and reproducibility**
- Include unit tests for windowing, preprocessing, model I/O, aggregation, API validation, and privacy-related data rejection.
- Provide documented commands for running the server and dashboard locally.
- Include a deterministic local/demo mode so the project can be tested without multiple physical phones.

### 2.2 Non-functional requirements

- **Privacy:** Minimize collected and transmitted data; keep raw traces local by default.
- **Security:** Use authenticated, encrypted transport for any non-local deployment; keep secrets out of source control.
- **Performance:** Avoid blocking the Android main thread; bound memory and background work.
- **Battery:** Use configurable sampling and work scheduling; measure rather than claim battery savings.
- **Reliability:** Handle network failures, server unavailability, client dropout, and malformed payloads.
- **Maintainability:** Keep Android, ML, FL server, reporting API, and dashboard components separated.
- **Reproducibility:** Pin compatible dependency versions and document setup steps.
- **Honesty:** Clearly distinguish implemented, tested, simulated, and planned functionality.

### 2.3 Acceptance criteria for the prototype

The project is considered an end-to-end prototype when:

1. The Android app can capture a bounded accelerometer window and display/debug its values.
2. The preprocessing pipeline has documented input dimensions and unit tests.
3. A baseline model can be evaluated on a documented dataset or clearly labelled sample dataset.
4. The app can load and run a compatible on-device inference model, or the project documents a tested alternative if on-device inference/training is blocked by platform constraints.
5. A Flower server and at least two test clients complete a verified federated round in a reproducible test environment. Android participation must be demonstrated only if a compatible Android client path has actually been verified.
6. The reporting API rejects precise coordinates and raw sensor payloads when the coarse-reporting design is enabled.
7. The dashboard displays aggregated demo/test hotspots and clearly marks the data source.
8. Automated tests and setup instructions pass on a clean environment.
9. The README lists known limitations and does not overstate privacy or model accuracy.

---

## 3. Reference Repositories and How to Use Them

The agent must inspect the current repository state and the **actual source files** in these references before deciding what to reuse. Treat repository names, READMEs, and this plan as leads—not proof that a particular API, file, or feature exists.

### Reference A — Pothole detection

- Repository: https://github.com/ht-eml/pothole-detection.git
- Potentially relevant areas: sensor collection, accelerometer processing, temporal windowing, event labelling, and baseline classification.
- Agent instructions:
  1. Inspect the README, directory tree, license, dependency versions, and relevant source files.
  2. Locate the actual sensor/windowing/model code before describing it.
  3. Reuse only components whose behavior and license are understood.
  4. Adapt concepts or code to the current Android architecture; do not copy a legacy cloud-upload path.
  5. Do not assume the repository uses the exact architecture or language described in this plan until verified.

### Reference B — Flower

- Repository: https://github.com/flwrlabs/flower.git
- Potentially relevant areas: Flower server strategy examples, client/server APIs, Android/Kotlin examples if present and currently supported, serialization, and integration tests.
- Agent instructions:
  1. Inspect the current `examples` tree and find the exact Android/Kotlin example, if it still exists.
  2. Read the example's README and source code and identify its Flower version and supported protocol.
  3. Verify compatibility with the server version selected for this project.
  4. Do not assume an older `FedAvgAndroid` class, Kotlin wrapper, or gRPC protocol is available in the current release.
  5. If Android is not supported by a maintained compatible client API, document the limitation and propose a small, testable alternative. Do not fabricate an Android client implementation.
  6. Verify whether local training with the selected model runtime is supported. TensorFlow Lite inference support does not by itself mean on-device training is supported.

### Code-retrieval policy

- Prefer upstream source files, tests, and examples over blog posts or remembered APIs.
- Record the exact source URL, file path, and relevant version/commit in `docs/REFERENCE_NOTES.md`.
- Follow the licenses and attribution requirements of reused code.
- Copy only the minimum required code; prefer adapting small, tested modules over importing entire repositories.
- Never paste large files from another repository without checking the license and necessity.
- If a reference cannot be fetched or verified, state that limitation and continue only with work that does not depend on the unverified claim.

---

## 4. Proposed Architecture

```text
Android phone
  ├── Consent and collection controls
  ├── Accelerometer capture and bounded window buffer
  ├── Preprocessing and local model inference
  ├── Optional local event storage / local training
  ├── Federated client adapter (only after protocol verification)
  └── Optional coarse hazard report
           │
           ├── FL transport: model parameters + required training metadata
           ▼
Python Flower server
  ├── Client coordination and FedAvg
  ├── Parameter/schema validation
  ├── Model version management
  └── Aggregation logs (no raw traces)

Optional coarse-report path
Android client ── minimal coarse report ──► Reporting API
                                             ├── Input validation
                                             ├── Minimum-count aggregation
                                             └── Aggregated hotspot data
                                                        │
                                                        ▼
                                               Leaflet dashboard
```

### Proposed technology choices

| Layer | Initial choice | Notes |
|---|---|---|
| Android | Kotlin, Android SDK, Gradle | Use the existing project structure if one already exists. |
| Sensor capture | Android `SensorManager` | Sampling and lifecycle behavior must be tested on a physical device. |
| Local inference | TensorFlow Lite/LiteRT-compatible runtime, subject to verification | Verify the currently supported library, model format, and Android API compatibility before implementation. |
| Model training | Python with a documented ML framework | Train/evaluate a baseline on the host first. |
| Federated orchestration | Flower (`flwr`) | Pin a version and verify its client/server APIs against the actual example. |
| Backend/report API | Python with Flask or FastAPI | Choose one after inspecting existing code; avoid unnecessary dual frameworks. |
| Dashboard | Leaflet + OpenStreetMap | Display only aggregated, privacy-filtered data. |
| Testing | Android/JVM tests and Python `pytest` where appropriate | Include unit, integration, and API validation tests. |

Do not introduce Kubernetes, cloud services, databases, authentication vendors, or complex infrastructure unless the project requirements justify them. Start locally and keep setup reproducible.

---

## 5. Data and Model Contract

Before implementing the model or client protocol, create `docs/MODEL_CONTRACT.md` and document:

- Sensor axes and units, including whether gravity is removed.
- Sampling frequency and handling of missing/irregular samples.
- Window length, overlap, trigger position, and padding behavior.
- Feature order, normalization constants, and expected tensor shape.
- Label set and meaning of each label.
- Model input/output tensor shapes and dtypes.
- Model version and compatibility rules.
- Local training method, if supported, and how training examples are labelled.
- Federated parameter ordering and schema validation.
- Test dataset source, license, class balance, and evaluation method.

**Do not invent model accuracy, dataset size, labels, tensor shapes, or performance numbers.** If these are not available, create a documented placeholder and resolve it during the relevant phase. Start with a binary baseline if data supports it; add multiple hazard classes only when a suitable labelled dataset and evaluation plan exist.

---

## 6. Privacy and Security Rules

1. Raw accelerometer windows and precise GPS coordinates stay on-device by default.
2. Do not upload raw video, route history, device advertising IDs, phone numbers, or unnecessary personal identifiers.
3. Federated updates are not guaranteed to be non-identifying. Document that risk.
4. Do not describe ordinary FedAvg as secure aggregation.
5. Use TLS and authentication for deployments beyond a trusted local test environment; never commit credentials or private keys.
6. Do not store precise coordinates and then merely hide them in the UI. Enforce data minimization at collection and API boundaries.
7. Coarse hotspot reports must use a documented grid size, minimum count, retention policy, and time bucket. Do not claim that coarse cells eliminate all re-identification risk.
8. Do not display a hotspot until the minimum report count is reached.
9. Separate synthetic/demo reports from real reports in storage, API responses, and the UI.
10. Add tests that confirm prohibited fields are rejected and raw sensor payloads are not logged.
11. Provide a user-facing explanation and controls for collection, pause, and local data deletion.
12. Treat any privacy guarantee not implemented and tested as a limitation, not as a completed feature.

---

## 7. Ten-Phase Implementation Plan

**Execution rule:** Work on exactly one phase at a time. After completing a phase, run its tests, summarize the changed files and evidence, commit the work, push the phase branch or agreed branch to the remote, and then **stop and ask the user to review and explicitly approve the next phase**. Do not automatically proceed to the next phase. If a push is rejected or credentials are unavailable, report the exact issue and do not claim the push succeeded.

### Phase 1 — Repository reconnaissance and implementation plan

- Inspect the current `Mini-Project` repository, branches, existing files, license, and Git status.
- Inspect both reference repositories and verify the exact files and APIs that may be reused.
- Identify the existing app/backend structure before proposing replacements.
- Create `docs/REFERENCE_NOTES.md`, `docs/IMPLEMENTATION_PLAN.md`, and an initial `README.md` update only if justified.
- Record risks, unknowns, dependency compatibility, and decisions needed.
- **Exit criteria:** Verified repository map, license notes, initial architecture, and a list of unresolved questions. No large-scale implementation yet.

### Phase 2 — Project skeleton, environment, and quality checks

- Establish or preserve a clean Android and Python project structure based on Phase 1 findings.
- Add dependency/version files and setup instructions.
- Add basic lint/test commands and `.gitignore` rules for build outputs, virtual environments, local data, secrets, and model files when appropriate.
- Add configuration examples without credentials.
- **Exit criteria:** A clean checkout can follow the documented setup, and basic checks run.

### Phase 3 — Sensor capture and temporal windowing

- Implement accelerometer capture with bounded buffers and configurable sampling/threshold values.
- Keep collection under explicit user control and respect Android lifecycle/background limits.
- Implement deterministic windowing and preprocessing.
- Add tests for threshold triggers, incomplete windows, sensor gaps, and invalid values.
- **Exit criteria:** The app can show or locally record a sample window; tests verify the data contract. No raw data is uploaded.

### Phase 4 — Dataset inspection and baseline model

- Find and verify usable labelled data from the reference repository or another clearly licensed source.
- If no suitable data exists, use a small clearly labelled synthetic dataset only to test the pipeline—not to claim real-world model quality.
- Train a simple baseline model in Python and record the split strategy and evaluation metrics.
- Add reproducible preprocessing and evaluation scripts.
- **Exit criteria:** A reproducible baseline and honest evaluation report exist, or a documented blocker explains what data is missing.

### Phase 5 — Mobile model conversion and local inference

- Convert the verified baseline to a supported mobile format only if conversion is technically supported.
- Verify the model's input/output contract and numerical consistency against the host model.
- Integrate local inference and show predicted class/confidence in a debug UI.
- Measure inference latency on an available test device; do not invent results.
- **Exit criteria:** A test input runs locally and output shape/labels are validated. If conversion fails, document the failure and alternative before continuing.

### Phase 6 — Flower server and Python test clients

- Choose and pin a Flower version after checking the current upstream examples.
- Implement the server strategy and schema/parameter validation.
- Implement at least two Python test clients to demonstrate a complete federated round before attempting Android integration.
- Test client dropout, invalid parameters, timeouts, and aggregation behavior.
- **Exit criteria:** Reproducible multi-client FL round with tests and logs that contain no raw sensor traces.

### Phase 7 — Android federated client feasibility and integration

- Re-check the exact Flower Android/Kotlin support and protocol compatibility documented in Phase 1.
- Implement the Android client only using a verified supported API/protocol.
- Confirm the chosen mobile runtime supports the required local training operation; inference-only support is insufficient.
- If the intended Android client is unsupported, stop and present a tested alternative for user approval rather than fabricating a wrapper.
- Validate parameter ordering, tensor shapes/dtypes, model versions, reconnects, and local training behavior.
- **Exit criteria:** Android joins a real test round, or a clear, evidence-backed platform limitation and approved alternative are documented.

### Phase 8 — Coarse hazard reporting API and privacy enforcement

- Implement a separate reporting endpoint for minimal coarse-cell reports if mapping is enabled.
- Validate allowed fields and reject precise coordinates, routes, raw sensor arrays, and unknown fields.
- Add configurable time buckets, retention, minimum-count aggregation, and demo/real data separation.
- Add automated privacy-boundary tests.
- **Exit criteria:** API tests prove prohibited data is rejected and low-count cells are withheld.

### Phase 9 — Municipal hotspot dashboard

- Implement the Leaflet map and minimal API integration.
- Show aggregated cells only, with legend, category, data freshness, minimum-count behavior, and a demo-data indicator.
- Add empty/error/loading states and avoid showing individual device activity.
- Verify map attribution and applicable tile usage rules.
- **Exit criteria:** Dashboard works locally against the API and does not expose raw location/event records.

### Phase 10 — End-to-end validation, documentation, and handoff

- Run all available automated tests and the documented clean-start procedure.
- Perform an end-to-end demo with synthetic/test events and multiple FL clients.
- Document architecture, setup, model contract, data flow, privacy limitations, troubleshooting, and known issues.
- Update the README with only features that were actually implemented and verified.
- Review Git diff for secrets, generated files, unrelated changes, and copied code/license compliance.
- **Exit criteria:** A reproducible demo, test evidence, accurate README, clean diff, and a final list of limitations.

---

## 8. GitHub Remote and Push Workflow

Target repository: https://github.com/agni-007/Mini-Project.git

The agent must not assume the local folder is already connected to this remote. Verify before changing anything.

1. Inspect `pwd`, `git status`, `git branch --show-current`, `git remote -v`, and recent commit history.
2. If this directory is not a Git repository, initialize it only after checking that doing so will not overwrite existing work.
3. If `origin` exists but points somewhere else, do **not** silently replace it. Report the mismatch and ask the user before changing the remote.
4. If no remote exists, add the target URL as `origin`.
5. Check the target branch and remote history before pushing. Do not force-push, rewrite history, or delete branches.
6. Prefer a phase-specific branch such as `agent/phase-01-recon` if branch permissions and project conventions allow. If the user explicitly expects changes directly on the default branch, inspect the branch and use a normal non-force push only when safe.
7. Make one focused commit per phase with a descriptive message, for example `phase 03: add accelerometer windowing`.
8. Push the phase branch/commit and verify the push result before reporting success.
9. If authentication, permissions, network access, branch protection, or a non-fast-forward rejection prevents the push, stop and report the exact error plus the safest next action. Never claim changes were pushed unless the Git command confirms it.
10. Never commit secrets, local datasets containing personal data, signing keys, credentials, build artifacts, or private logs.

The agent may run safe read-only Git commands without asking. It must not discard user changes, force-push, overwrite unrelated remote history, or change a mismatched remote without explicit approval.

---

## 9. Mandatory Master Prompt for the AI Coding Agent

Copy the following prompt into the coding agent that has access to the local workspace, terminal, and GitHub credentials.

```text
You are the implementation agent for FedRoad-Net, a student mini-project. Your job is to build it incrementally, verify every claim, use the reference repositories efficiently, and keep the GitHub repository in a reviewable state.

TARGET REPOSITORY
https://github.com/agni-007/Mini-Project.git

REFERENCE REPOSITORIES
https://github.com/ht-eml/pothole-detection.git
https://github.com/flwrlabs/flower.git

SOURCE OF TRUTH
1. This implementation.md file is the project requirements and phase plan.
2. The actual current workspace and Git history are the source of truth for existing implementation.
3. The current contents of the reference repositories are evidence only after you inspect them.
4. Do not assume that APIs, classes, folders, protocols, model files, or features exist merely because this document mentions them as possibilities.

NON-NEGOTIABLE WORKFLOW
- Execute only one phase at a time, starting with Phase 1.
- At the end of every phase, run the relevant checks, inspect the diff, commit and push safely where possible, then STOP.
- After stopping, tell me what changed and ask: “Please review Phase N. Should I continue to Phase N+1?”
- Do not begin the next phase until I explicitly approve it.
- Do not batch all phases into one run, and do not treat silence as approval.
- If a phase is blocked by missing hardware, unavailable credentials, unsupported libraries, absent data, or an unclear requirement, explain the evidence and ask a focused question rather than inventing a solution.

ANTI-HALLUCINATION RULES
- Inspect files before referring to their contents. Cite exact repository URLs, paths, version numbers, or commit IDs in docs/REFERENCE_NOTES.md.
- Before using any library API, verify it against the installed/pinned version and official source or a working upstream example.
- Never fabricate model accuracy, dataset contents, benchmark numbers, test results, screenshots, file paths, classes, APIs, or successful Git pushes.
- Never report a test as passing unless you actually ran it and saw the result.
- Distinguish clearly between implemented, tested, simulated, planned, and blocked work.
- When uncertain, write down the uncertainty and investigate. Do not fill gaps with plausible-sounding code.
- Prefer the smallest working implementation over speculative architecture or unnecessary abstractions.
- Do not silently change the product requirements or remove a feature to make the build appear successful.

REFERENCE CODE RETRIEVAL
- Inspect both reference repositories' README, directory tree, license, dependency versions, and relevant implementation/tests.
- Search for the exact sensor capture/windowing logic in ht-eml/pothole-detection and the actual supported Flower client/server examples in flwrlabs/flower.
- Reuse only code that exists and whose license is compatible with this project; record attribution and exact source path.
- Prefer adapting focused modules over cloning entire projects into this repository.
- Do not assume the Flower Android/Kotlin example is still supported. Verify its current status and protocol.
- Do not assume TensorFlow Lite supports training merely because it supports inference. Verify the chosen on-device training route.
- If the intended Android FL client is not supported, present an evidence-backed alternative and wait for approval before changing the architecture.

PRIVACY AND SECURITY
- Keep raw accelerometer windows and precise GPS traces on-device by default.
- Do not upload raw video, route histories, precise coordinates, or raw sensor arrays.
- Model updates are not inherently private, and TLS is not the same as secure aggregation. Never claim otherwise.
- The map needs a separate coarse reporting path; do not pretend model weights alone locate potholes.
- Reject precise coordinates and raw sensor payloads at the reporting API boundary.
- Do not commit credentials, secrets, signing keys, private data, or local build outputs.
- Keep synthetic/demo data visibly labelled and separate from real reports.
- Do not make production-security claims unless the feature is implemented and tested.

GIT SAFETY
- Inspect the workspace and Git state before editing.
- Confirm that the target remote is https://github.com/agni-007/Mini-Project.git. Do not silently replace a different existing remote.
- Never force-push, reset away user work, discard uncommitted changes, or rewrite remote history.
- Make a focused commit for each approved phase and push normally to a safe branch/target.
- Verify command output before saying the commit or push succeeded.
- If authentication, permissions, or branch conflicts prevent a push, stop and report the exact error. Do not fake success.

PHASE COMPLETION REPORT
At the end of each phase, provide:
1. Phase number and objective.
2. What was implemented and what was deliberately not implemented.
3. Files created/modified, with a short explanation.
4. Reference files and APIs verified, with source URLs/paths.
5. Commands/tests actually run and their real results.
6. Known limitations, unresolved questions, and risks.
7. Git branch, commit hash, remote, and push status (only if verified).
8. The exact next phase you propose.
9. Ask me to review and explicitly approve continuing. Then stop.

BEGIN NOW
Start with Phase 1 only. Inspect the workspace and remote state, inspect both reference repositories, verify licenses and relevant APIs, and produce the reconnaissance notes and implementation plan. Do not implement all ten phases. Do not start Phase 2 until I approve Phase 1.
```

---

## 10. Recommended Repository Layout

Adapt this to the repository after reconnaissance; do not move or overwrite existing files just to match this example.

```text
Mini-Project/
├── README.md
├── implementation.md
├── docs/
│   ├── REFERENCE_NOTES.md
│   ├── IMPLEMENTATION_PLAN.md
│   ├── MODEL_CONTRACT.md
│   ├── PRIVACY_AND_DATA_FLOW.md
│   └── TESTING.md
├── android-app/             # Or preserve the existing Android project location
├── ml/
│   ├── data/                # Keep private/local data out of Git
│   ├── train.py
│   ├── evaluate.py
│   └── convert_model.py
├── fl-server/
│   ├── server.py
│   ├── clients/              # Python test clients if needed
│   └── tests/
├── reporting-api/
│   ├── app/
│   └── tests/
└── dashboard/
```

---

## 11. Final Definition of Done

The project is ready for academic demonstration when:

- The main data flow and model contract are documented.
- Sensor capture and preprocessing are tested.
- Model inference is verified on a supported target, or a blocker and approved alternative are documented.
- Federated aggregation is demonstrated with reproducible clients; Android FL participation is claimed only if verified.
- The coarse reporting API enforces data minimization and minimum-count aggregation.
- The dashboard shows aggregated demo hotspots without exposing individual routes or raw events.
- Setup instructions work from a clean checkout.
- The README describes actual verified behavior and limitations.
- Each phase has a reviewable commit, and the remote push status is confirmed.
- The agent has paused for human review after every phase.
