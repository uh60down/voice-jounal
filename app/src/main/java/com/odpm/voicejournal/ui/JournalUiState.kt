package com.odpm.voicejournal.ui

import com.odpm.voicejournal.domain.model.PlaybackState
import com.odpm.voicejournal.domain.model.VoiceRecord
import com.odpm.voicejournal.domain.model.VoiceRecordId

/**
 * ODPM application states, expressed as one exclusive mode:
 * Idle / Recording / Selection Mode / Metadata Editing.
 *
 * The fifth ODPM application state, Playback, is carried separately in
 * [JournalUiState.playback] rather than as a mode: listening does not lock
 * the journal, so the user can keep browsing (and even enter selection)
 * while a record plays. A record is in its "Playing" state exactly when
 * playback references its id; every other visible record is "Saved".
 */
sealed interface JournalMode {
    data object Idle : JournalMode

    /** ODPM state: Recording. [startedAtElapsedRealtime] drives the elapsed-time ticker. */
    data class Recording(val startedAtElapsedRealtime: Long) : JournalMode

    /** ODPM state: Selection Mode. [confirmingDelete] shows the delete confirmation. */
    data class Selection(
        val selected: Set<VoiceRecordId>,
        val confirmingDelete: Boolean = false,
    ) : JournalMode

    /** ODPM state: Metadata Editing. */
    data class MetadataEditing(val record: VoiceRecord) : JournalMode
}

data class JournalUiState(
    /** ODPM concept: VoiceRecordList (displays → VoiceRecord). */
    val records: List<VoiceRecord> = emptyList(),
    val mode: JournalMode = JournalMode.Idle,
    val playback: PlaybackState = PlaybackState.Idle,
    val message: String? = null,
)
