# ODPM Learning Log — Voice Journal MVP

Implementation observations feeding back into [the ontology](ODPM.md).
In ODPM, the ontology is a living shared understanding: findings recorded
here are candidates to be folded back into the ontology document once agreed.

| # | Finding | Status |
|---|---|---|
| F1 | Playback is not an exclusive App State | **WIP** |
| F2 | Paused is not supported in MVP | Backlog |
| F3 | Stop Recording means Save | Backlog |
| F4 | Recording and Playback conflict | Backlog |
| F5 | Deleting a playing record | Backlog |

---

## Finding 1 — Playback is not an exclusive App State (WIP)

### Observation

Playback can run alongside other application modes.

The user may continue browsing the VoiceRecordList or enter Selection Mode
while a VoiceRecord is playing.

### Ontology Impact

`Playback` should not be modeled only as an exclusive Application State.

Better model:

- App Mode: `Idle`, `Recording`, `SelectionMode`, `MetadataEditing`
- Playback State: `Stopped`, `Playing`

### Rule Candidate

Playback may continue while the user browses or selects records, unless the
selected action conflicts with the playing record.

### Implementation trace

The code already follows the proposed model: `JournalMode` carries the four
exclusive app modes, while playback travels separately as
`JournalUiState.playback` (`ui/JournalUiState.kt`), backed by the domain's
`PlaybackState` (`Idle`/`Playing` — to be renamed `Stopped`/`Playing` if the
finding lands with that vocabulary).

Two "conflicting actions" are handled today, and are themselves findings
F4 and F5; the general conflict clause is the open (WIP) part of this rule.

---

## Finding 2 — Paused is not supported in MVP (Backlog)

### Observation

The ontology lists `Paused`, but the MVP defines only play and stop behavior.

There is no Pause action.

### Ontology Impact

Remove `Paused` from MVP states, or move it to Future Evolution.

### Rule Candidate

MVP playback supports only `Playing` and `Stopped`.

Pause may be introduced later as an explicit behavior.

### Implementation trace

`PlaybackState` deliberately models no Paused state
(`domain/model/PlaybackState.kt`); introducing Pause later is an additive
change (new state + new behavior), not a modification of existing ones.

---

## Finding 3 — Stop Recording means Save (Backlog)

### Observation

The ontology defines Stop Recording and Save VoiceRecord, but no Discard
behavior.

Therefore, stopping a recording always saves the VoiceRecord.

### Ontology Impact

Clarify the relationship between Stop Recording and Save VoiceRecord.

### Rule Candidate

In MVP, stopping a recording creates and saves a VoiceRecord automatically.

Discard is excluded unless explicitly added as a future behavior.

### Implementation trace

`StopRecordingUseCase` and `SaveVoiceRecordUseCase` remain distinct (they are
distinct ontology behaviors), but the ViewModel always chains them
(`JournalViewModel.stopAndSaveRecording`) — there is no UI path that stops
without saving.

---

## Finding 4 — Recording and Playback conflict (Backlog)

### Observation

Starting a new recording stops any active playback.

### Ontology Impact

Recording and playback are not independent states.

Recording has priority over playback.

### Rule Candidate

Starting a recording must stop active playback before recording begins.

### Implementation trace

`StartRecordingUseCase` calls `player.stop()` before `recorder.start()`;
covered by the test "starting a recording stops any current playback"
(`CaptureFirstRulesTest`).

---

## Finding 5 — Deleting a playing record (Backlog)

### Observation

If the user deletes a VoiceRecord that is currently playing, playback must
stop first.

### Ontology Impact

Delete behavior has a side effect when applied to the currently playing
VoiceRecord.

### Rule Candidate

Deleting the currently playing VoiceRecord must stop playback before deletion.

### Implementation trace

`DeleteVoiceRecordsUseCase` stops the player when the playing record's id is
in the deletion set; covered by the tests "deleting the record that is
playing stops playback first" and "deleting other records leaves current
playback alone" (`OrganizeLaterRulesTest`).
