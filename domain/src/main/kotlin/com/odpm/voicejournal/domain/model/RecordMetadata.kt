package com.odpm.voicejournal.domain.model

import java.time.Instant

/**
 * ODPM concept: RecordMetadata — optional information describing a VoiceRecord.
 *
 * Rule R2 (Metadata is Optional): [title] and [description] are nullable and
 * default to null. A VoiceRecord is fully valid without either.
 *
 * The attribute set (title, description, createdAt, updatedAt, duration)
 * follows the ODPM document verbatim — including duration living here
 * rather than on AudioFile.
 */
data class RecordMetadata(
    val title: String? = null,
    val description: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val durationMillis: Long,
) {
    init {
        require(durationMillis >= 0) { "duration must not be negative" }
    }
}
