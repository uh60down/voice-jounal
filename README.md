# Voice Journal — an ODPM sample app

A minimal Android voice journal built as the first sample of the
**Ontology-Driven Practice Model (ODPM)**: understand the domain first, then let the
implementation express that understanding.

> «Capture first. Organize later.»

- **[docs/ODPM.md](docs/ODPM.md)** — the ontology (concepts, relationships, behaviors,
  states, rules R1–R9). This is the source of truth.
- **[docs/IMPLEMENTATION-MAPPING.md](docs/IMPLEMENTATION-MAPPING.md)** — how every
  implementation decision traces back to that document.
- **[docs/LEARNING-LOG.md](docs/LEARNING-LOG.md)** — findings flowing back from
  implementation into the ontology (F1 in progress, F2–F5 backlog).

## What the MVP does

- Record a voice note immediately — no title, no tags, no AI, nothing in the way (R1)
- Stop and save; browse the journal newest-first
- Play / stop a saved record (R5)
- Edit title and description later, or never (R2, R3, R6)
- Delete one or multiple records via long-press selection (R7)

Explicitly excluded (future ontology extensions, R9): AI transcription/summaries/titles,
search, tags, cloud sync, accounts.

## Structure

| Module | Role |
|---|---|
| `:domain` | The ontology as pure Kotlin — models, ports, one use case per ODPM action, rules enforced and unit-tested. No Android dependency. |
| `:app` | The Android expression — Jetpack Compose UI, Room persistence, MediaRecorder/MediaPlayer adapters, manual DI. |

## Build & run

Requires an Android SDK (Android Studio, or `ANDROID_HOME` with platform 35).

```sh
./gradlew :app:assembleDebug     # build the APK
./gradlew :domain:test           # run the ontology rule tests (no Android SDK needed)
```

Min SDK 26, target SDK 35, Kotlin 2.1, Compose (Material 3), Room.

> Note: this repository was authored in a sandbox without access to the Android SDK
> distribution, so `:domain` (all ontology logic and rules, 18 tests) is compile- and
> test-verified, while `:app` has not been compiled here. If a dependency/API mismatch
> surfaces on first local build, it should be a version-bump fix, not a design change.
