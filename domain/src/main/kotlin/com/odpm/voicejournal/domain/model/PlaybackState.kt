package com.odpm.voicejournal.domain.model

/**
 * ODPM states, playback aspect: a saved VoiceRecord is "Playing" while it is
 * the record referenced here, otherwise it is simply "Saved".
 *
 * The ontology also lists a "Paused" state, but the MVP behavior list
 * contains no Pause action (only Play VoiceRecord / Stop Playback), so
 * Paused is intentionally not modeled yet — it can be added as a new state
 * without changing the meaning of the existing ones.
 */
sealed interface PlaybackState {
    data object Idle : PlaybackState
    data class Playing(val recordId: VoiceRecordId) : PlaybackState
}
