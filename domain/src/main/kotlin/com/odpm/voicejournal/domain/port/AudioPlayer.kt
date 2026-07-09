package com.odpm.voicejournal.domain.port

import com.odpm.voicejournal.domain.model.PlaybackState
import com.odpm.voicejournal.domain.model.VoiceRecord
import kotlinx.coroutines.flow.StateFlow

/**
 * Playback port. Implemented in the app layer with the device media player.
 *
 * [play] takes a full [VoiceRecord] — not a bare uri — so that only records
 * that actually exist can reach the player (rule R5: only saved VoiceRecords
 * may be played; the lookup happens in PlayVoiceRecordUseCase).
 */
interface AudioPlayer {
    val playback: StateFlow<PlaybackState>

    /** ODPM action: Play VoiceRecord. Starting a new record replaces the current one. */
    suspend fun play(record: VoiceRecord)

    /** ODPM action: Stop Playback. Safe to call when nothing is playing. */
    suspend fun stop()
}
