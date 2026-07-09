# Implementation Mapping — ODPM Ontology → Code

This document is the bridge between [the ODPM document](ODPM.md) (the source of truth)
and the code in this repository. It was produced *before* implementation, following the
ODPM order: concepts → relationships → behaviors → states → rules. Screens and database
tables appear last, as consequences of the ontology — never as the starting point.

## 0. Architectural consequence of ODPM

The ontology is independent of Android, so the code that expresses it is too:

- **`:domain`** — a pure Kotlin module (no Android dependency). It contains the five
  ontology elements as code: concepts (`model/`), relationships and behaviors
  (`usecase/`, `port/`), states (`model/PlaybackState.kt`, recorder/player state flows),
  and rules (enforced inside models and use cases, verified by unit tests).
- **`:app`** — the Android expression of that ontology: Compose UI, Room persistence,
  and microphone/speaker adapters. Everything here *implements* a domain port or
  *renders* a domain concept; nothing here defines domain meaning.

This is the ODPM claim made concrete: the ontology is the stable center, and the app is
"simply an implementation of that understanding."

## 1. Concepts → domain models

| ODPM concept | Code | Notes |
|---|---|---|
| VoiceRecord | `domain/model/VoiceRecord.kt` | The central concept. Aggregates identity + audio + metadata. |
| AudioFile | `domain/model/AudioFile.kt` | Value object holding the audio uri. |
| RecordMetadata | `domain/model/RecordMetadata.kt` | title, description, createdAt, updatedAt, duration — the attribute list follows the ODPM document verbatim (duration lives here, not on AudioFile). |
| VoiceRecordList | `ui/JournalUiState.kt` (`records`) + the `LazyColumn` in `JournalScreen.kt` | A *presentation* concept: "a collection that displays existing VoiceRecords." It has no behavior of its own, so it maps to UI state + a list component rather than a domain class. |
| User | The person holding the phone | Single-user, no login (excluded from MVP), so the User is the actor driving the UI, not a stored entity. Every ViewModel entry point is a User relationship from the ontology. |

Supporting types that fall out of the behaviors (not new concepts): `VoiceRecordId`
(identity), `RecordedAudio` (the hand-off between *Stop Recording* and *Save
VoiceRecord*), `PlaybackState` (the playing/idle state aspect).

## 2. Relationships → use cases / interactions

| ODPM relationship | Code |
|---|---|
| User creates → VoiceRecord | `StartRecordingUseCase` → `StopRecordingUseCase` → `SaveVoiceRecordUseCase` |
| User selects → VoiceRecord | `JournalViewModel.enterSelection` / `toggleSelection` (Selection Mode) and row taps |
| User plays → VoiceRecord | `PlayVoiceRecordUseCase` |
| User stops → VoiceRecord | `StopPlaybackUseCase` (and `StopRecordingUseCase` for the capture side) |
| User updates → VoiceRecord | `UpdateMetadataUseCase` |
| User deletes → VoiceRecord | `DeleteVoiceRecordsUseCase` |
| VoiceRecord contains → AudioFile | Non-nullable field `VoiceRecord.audio` (composition: deleting the record deletes the file) |
| VoiceRecord has → RecordMetadata | Non-nullable field `VoiceRecord.metadata` with nullable contents (R2) |
| VoiceRecordList displays → VoiceRecord | `VoiceRecordRepository.observeAll()` → `ObserveVoiceRecordsUseCase` → `JournalUiState.records` → `VoiceRecordRow` |

## 3. Actions → app behavior

| ODPM action | Domain | UI trigger |
|---|---|---|
| Record Voice | `StartRecordingUseCase` | Mic FAB tap (asks mic permission just-in-time, nothing else — R1) |
| Stop Recording | `StopRecordingUseCase` | Stop FAB tap while recording |
| Save VoiceRecord | `SaveVoiceRecordUseCase` | Runs immediately after Stop Recording — saving never asks for input |
| View VoiceRecordList | `ObserveVoiceRecordsUseCase` | The screen itself (reactive Flow, newest first) |
| Select VoiceRecord | — (application-state concern) | Long-press enters Selection Mode; taps toggle |
| Play VoiceRecord | `PlayVoiceRecordUseCase` | Play icon / row tap |
| Stop Playback | `StopPlaybackUseCase` | Stop icon / row tap while playing |
| Update Metadata | `UpdateMetadataUseCase` | Edit icon → Metadata Editing dialog |
| Delete VoiceRecord(s) | `DeleteVoiceRecordsUseCase` | Selection Mode → delete icon → confirmation |

## 4. States → UI/application state

Application states (mutually exclusive) map to `JournalMode`:

| ODPM application state | Code |
|---|---|
| Idle | `JournalMode.Idle` |
| Recording | `JournalMode.Recording` (driven by `AudioRecorder.isRecording`) |
| Playback | `JournalUiState.playback` — carried *alongside* the mode, see deviations below |
| Selection Mode | `JournalMode.Selection` |
| Metadata Editing | `JournalMode.MetadataEditing` |

VoiceRecord states map as follows:

| ODPM VoiceRecord state | Where it lives |
|---|---|
| Recording | Transient: the active session inside `MediaRecorderAudioRecorder` (a record does not exist yet — it becomes a VoiceRecord at save) |
| Saved | Every row in the repository/list |
| Playing | `PlaybackState.Playing(recordId)` — the record whose id playback references |
| Paused | **Not modeled** — the ontology lists the state but defines no Pause behavior, and the MVP scope has only Play/Stop. Adding it later is a new state, not a change to existing ones. |
| Updating | The record held by `JournalMode.MetadataEditing` while the dialog is open |
| Deleted | Absence: the record and its audio are removed (no tombstones in the MVP) |

## 5. Rules → validation and constraints

| Rule | Enforcement point | Test |
|---|---|---|
| R1 Capture First | `StartRecordingUseCase` takes **no arguments** — metadata cannot even be passed. UI: mic FAB starts capture directly; the only possible gate is the OS mic permission. | `CaptureFirstRulesTest` |
| R2 Metadata is Optional | `RecordMetadata.title/description` nullable with null defaults; `UpdateMetadataUseCase` normalizes blank → null | `CaptureFirstRulesTest`, `OrganizeLaterRulesTest` |
| R3 Default Display Name | `VoiceRecord.displayName()` — "Untitled Log — YYYY/MM/DD HH:mm" | `DisplayNameRuleTest` |
| R4 Audio is Required | Structural: `VoiceRecord.audio` is non-nullable and `AudioFile` rejects blank uris — a record without audio cannot be represented | `CaptureFirstRulesTest` |
| R5 Playback (saved only) | `PlayVoiceRecordUseCase` resolves the id through the repository; unsaved ids throw `VoiceRecordNotFoundException` | `PlaybackRulesTest` |
| R6 Metadata Editing (audio immutable) | `VoiceRecordRepository.updateMetadata` is the only mutation and can only touch title/description/updatedAt; no API anywhere replaces an AudioFile | `OrganizeLaterRulesTest` |
| R7 Deletion (one or many) | `DeleteVoiceRecordsUseCase(ids: Set<VoiceRecordId>)`; UI multi-select via Selection Mode | `OrganizeLaterRulesTest` |
| R8 Single Recording Session | `StartRecordingUseCase` throws `RecordingAlreadyActiveException` when a session is active; `MediaRecorderAudioRecorder` physically holds at most one session | `CaptureFirstRulesTest` |
| R9 AI is an Extension | Nothing to enforce — there is deliberately no AI code, no stub, no placeholder in the capture path. Future concepts (Transcript, Insight, SearchIndex) arrive as new models + use cases without touching existing ones. | (by absence) |

## 6. Android package structure

```
voice-journal/
├── domain/                                  # the ontology as pure Kotlin
│   └── com.odpm.voicejournal.domain
│       ├── model/        VoiceRecord, AudioFile, RecordMetadata,
│       │                 VoiceRecordId, RecordedAudio, PlaybackState
│       ├── port/         VoiceRecordRepository, AudioRecorder, AudioPlayer
│       ├── usecase/      one class per ODPM action
│       └── DomainErrors  rule violations as typed exceptions
└── app/                                     # the Android expression
    └── com.odpm.voicejournal
        ├── VoiceJournalApp                  # manual DI (AppContainer)
        ├── data/
        │   ├── db/       Room: VoiceRecordEntity, VoiceRecordDao, JournalDatabase
        │   ├── audio/    MediaRecorderAudioRecorder, MediaPlayerAudioPlayer
        │   └── VoiceRecordRepositoryImpl
        └── ui/
            ├── MainActivity, JournalScreen, JournalViewModel,
            │   JournalUiState (JournalMode = application states)
            ├── components/  VoiceRecordRow, RecordingBanner, MetadataEditDialog
            └── theme/
```

## 7. Data model

Domain (the ontology's shape):

```
VoiceRecord(id, audio: AudioFile, metadata: RecordMetadata)
AudioFile(uri)
RecordMetadata(title?, description?, createdAt, updatedAt, durationMillis)
```

Persistence (one Room table — an implementation detail that flattens the
composition; mapping functions restore the domain shape so nothing outside
`data/db` ever sees the flat row):

```
voice_records(id PK, audioUri, title?, description?,
              createdAtEpochMillis, updatedAtEpochMillis, durationMillis)
```

Audio bytes live as AAC/MP4 files in app-private storage
(`filesDir/recordings/<uuid>.m4a`) — no storage permission, nothing between the
user and capture (R1).

## 8. UI screens/components

One screen (`JournalScreen`), because the MVP has one place: the journal.

- **Mic FAB** — Record Voice; becomes Stop while recording (Stop Recording + Save).
- **RecordingBanner** — the visible Recording state: elapsed time only, no input fields.
- **VoiceRecordList** (`LazyColumn` of `VoiceRecordRow`) — display name (R3),
  captured-at + duration, description preview, play/stop, edit.
- **Selection top bar** — Selection Mode: count, exit, delete (with confirmation, R7).
- **MetadataEditDialog** — Metadata Editing: title + description only (R6).
- **Empty state** — "Tap the mic and just talk. You can name it later — or never."

## 9. Decisions, questions, and risks

Decisions taken where the ODPM document leaves room. Decisions 1–5 have been
promoted to ontology findings in the [Learning Log](LEARNING-LOG.md)
(F1 in progress, F2–F5 backlog):

1. **Playback is carried alongside the application mode, not as an exclusive mode**
   (→ [F1](LEARNING-LOG.md), WIP).
   Listening shouldn't lock the journal — the user can browse or enter Selection Mode
   while audio plays. "The application is in the Playback state" ⇔ `playback` is
   `Playing`.
2. **Paused is not modeled** (→ [F2](LEARNING-LOG.md), see §4): the ontology lists the
   state but no Pause action exists in Behaviors, and MVP scope says play/stop only.
3. **Stop Recording always saves** (→ [F3](LEARNING-LOG.md)). The ontology has no
   "discard recording" behavior, and capture-first implies a captured thought is worth
   keeping. (A capture too short to contain audio is discarded by the recorder with an
   error, since R4 makes a record without audio unrepresentable.)
4. **Starting a recording stops playback** (→ [F4](LEARNING-LOG.md)) — the microphone
   wins over the speaker so a new thought is never captured over another one playing aloud.
5. **Deleting a playing record stops playback first** (→ [F5](LEARNING-LOG.md)) — a
   deleted record has no states left, including Playing.
6. **Duration is measured as wall-clock session time**, not decoded from the media file.
   Simple and permission-free; could drift a few hundred ms from the true audio length.
7. **Deletion is reached through Selection Mode only** (long-press → select → delete),
   which covers "one or multiple" (R7) with a single interaction model.

Risks:

- **`MediaRecorder.stop()` fails on near-instant recordings** — handled by discarding
  the session (nothing was captured), surfaced as a snackbar.
- **Room + Compose versions were chosen without a compile check against the Android
  SDK in this environment** (see README build note); any mismatch is a version bump,
  not a design change.
- **No audio focus handling in the MVP** — another app's audio may mix with playback.
  A future `AudioFocus` concern belongs in the player adapter, not the domain.
- **Process death during recording** loses the in-flight session (the file remains but
  is not indexed). Acceptable for MVP; a recovery sweep at startup is a future addition.
