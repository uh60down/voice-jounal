# ODPM Example — Voice Journal MVP

«A complete example of applying the Ontology-Driven Practice Model (ODPM) to a small but realistic product.»

---

## About ODPM

Ontology-Driven Practice Model (ODPM) is a discipline for cultivating shared understanding before taking action.

Instead of beginning with implementation artifacts—such as user interfaces, APIs, database schemas, or software architecture—ODPM begins by understanding the domain itself.

Every practice starts with the same five questions:

1. What concepts exist?
2. How are those concepts related?
3. What behaviors are allowed?
4. What states can those concepts be in?
5. What rules govern the domain?

The resulting ontology becomes the shared understanding that guides every subsequent decision—from product discovery and software design to implementation and future evolution.

---

## Why This Example?

A voice journal is intentionally simple.

Most people can understand its purpose within minutes, making it an ideal example for demonstrating the complete ODPM process without the complexity of an enterprise system.

Although the product is small, it contains every essential element of ODPM:

- Concepts
- Relationships
- Behaviors
- States
- Rules

---

## Product Intention

The product helps users capture short voice thoughts before they are forgotten.

The guiding principle is:

«Capture first. Organize later.»

Recording should never be blocked by titles, descriptions, tags, AI processing, or other metadata.

## User Stories

### Story 1 — Capture an Idea

As a user, I want to record a voice note immediately, so that I can capture an idea before I forget it.

### Story 2 — Revisit My Thoughts

As a user, I want to browse and play my previous voice records, so that I can revisit my thoughts whenever I need them.

### Story 3 — Organize My Records

As a user, I want to edit the title or description and remove voice records when necessary, so that I can keep my journal organized over time.

Together, these three user stories define the MVP scope:

1. Capture thoughts quickly.
2. Retrieve thoughts easily.
3. Organize thoughts when appropriate.

Everything else—including AI transcription, summarization, semantic search, intelligent tagging, and cloud synchronization—is considered a future enhancement rather than part of the MVP.

---

## Concepts (Entities)

### User

The person who records and manages voice thoughts.

### VoiceRecord

A single captured voice thought.

This is the central concept of the domain.

### AudioFile

The recorded audio associated with a VoiceRecord.

### RecordMetadata

Optional information describing a VoiceRecord.

Typical attributes include:

- title
- description
- createdAt
- updatedAt
- duration

### VoiceRecordList

A collection that displays existing VoiceRecords.

---

## Relationships (Links)

### User

- creates → VoiceRecord
- selects → VoiceRecord
- plays → VoiceRecord
- stops → VoiceRecord
- updates → VoiceRecord
- deletes → VoiceRecord

### VoiceRecord

- contains → AudioFile
- has → RecordMetadata

### VoiceRecordList

- displays → VoiceRecord

---

## Actions (Behaviors)

- Record Voice
- Stop Recording
- Save VoiceRecord
- View VoiceRecordList
- Select VoiceRecord
- Play VoiceRecord
- Stop Playback
- Update Metadata
- Delete VoiceRecord(s)

---

## States

### VoiceRecord

- Recording
- Saved
- Playing
- Paused
- Updating
- Deleted

### Application

- Idle
- Recording
- Playback
- Selection Mode
- Metadata Editing

---

## Rules

### R1 — Capture First

Recording begins immediately.

The user is never required to provide:

- title
- description
- tags
- AI summary
- transcription

before recording.

### R2 — Metadata is Optional

A VoiceRecord may exist without a title or description.

### R3 — Default Display Name

When no title is provided, display:

"Untitled Log — YYYY/MM/DD HH:mm"

### R4 — Audio is Required

Every VoiceRecord contains exactly one AudioFile.

### R5 — Playback

Only saved VoiceRecords may be played.

### R6 — Metadata Editing

Title and description may be updated after recording.

The audio itself is immutable in the MVP.

### R7 — Deletion

Users may delete one or multiple VoiceRecords.

### R8 — Single Recording Session

Only one recording session may be active at any time.

### R9 — AI is an Extension

Capabilities such as:

- transcription
- summarization
- title generation
- semantic search
- tagging

are future extensions and must never interrupt the primary recording workflow.

---

## Domain Ontology

```
                     User
                       │
        ┌──────────────┼──────────────┐
        │              │              │
     creates       selects        updates
        │              │              │
        ├──────────────┼──────────────┤
        │              │              │
      plays         stops         deletes
                       │
                       ▼
                 VoiceRecord
                 /          \
                /            \
         contains             has
             │                 │
             ▼                 ▼
       AudioFile       RecordMetadata

VoiceRecordList
        │
   displays
        │
        ▼
   VoiceRecord
```

---

## Future Evolution

The ontology remains stable even as the product evolves.

New capabilities are introduced by adding new concepts and relationships rather than modifying existing ones.

Examples:

```
VoiceRecord
    └── has ─────────► Transcript

VoiceRecord
    └── produces ────► Insight

VoiceRecord
    └── indexed by ──► SearchIndex
```

This allows the product to grow without changing the meaning of its core concepts.

---

## ODPM Reflection

This example demonstrates the central philosophy of the Ontology-Driven Practice Model (ODPM).

A conventional software process typically starts by discussing screens, APIs, database tables, or implementation details.

ODPM starts somewhere else.

It begins by building a shared understanding of the domain.

Once the concepts, relationships, behaviors, states, and rules are understood, implementation becomes a matter of expressing that understanding through software.

In this example, VoiceRecord naturally emerged as the central concept—not because of a database design or class hierarchy, but because every meaningful interaction in the domain revolves around it.

The resulting ontology is small, stable, and easy to evolve. New capabilities such as AI transcription, summarization, semantic search, or intelligent insights can be introduced by adding new concepts and relationships without changing the meaning of the existing model.

This illustrates a key principle of ODPM:

«Understand the domain before designing the solution.»

The ontology is not another project artifact.

It is the shared understanding that connects intention, design, implementation, and future evolution.

Everything else is simply an implementation of that understanding.
