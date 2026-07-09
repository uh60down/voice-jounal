package com.odpm.voicejournal.domain.model

import java.time.Instant

/**
 * The output of a finished recording session, before it is saved as a
 * [VoiceRecord]. This is the hand-off between the ODPM actions
 * "Stop Recording" and "Save VoiceRecord".
 */
data class RecordedAudio(
    val uri: String,
    val durationMillis: Long,
    val recordedAt: Instant,
)
