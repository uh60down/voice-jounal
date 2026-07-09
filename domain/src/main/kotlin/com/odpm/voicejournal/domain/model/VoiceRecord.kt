package com.odpm.voicejournal.domain.model

import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * ODPM concept: VoiceRecord — a single captured voice thought.
 *
 * This is the central concept of the domain; every behavior in the
 * ontology (create, select, play, stop, update, delete) revolves around it.
 *
 * Relationships from the ontology map to fields:
 *  - VoiceRecord contains → AudioFile      ([audio], non-null: rule R4)
 *  - VoiceRecord has      → RecordMetadata ([metadata], contents optional: rule R2)
 */
data class VoiceRecord(
    val id: VoiceRecordId,
    val audio: AudioFile,
    val metadata: RecordMetadata,
) {
    /**
     * Rule R3 (Default Display Name): when no title is provided, display
     * "Untitled Log — YYYY/MM/DD HH:mm" derived from the creation time.
     */
    fun displayName(zone: ZoneId = ZoneId.systemDefault()): String {
        val title = metadata.title
        if (!title.isNullOrBlank()) return title
        return "Untitled Log — ${DEFAULT_NAME_FORMATTER.format(metadata.createdAt.atZone(zone))}"
    }

    private companion object {
        val DEFAULT_NAME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    }
}
