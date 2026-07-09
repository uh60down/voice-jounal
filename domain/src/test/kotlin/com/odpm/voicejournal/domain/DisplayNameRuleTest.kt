package com.odpm.voicejournal.domain

import com.odpm.voicejournal.domain.model.AudioFile
import com.odpm.voicejournal.domain.model.RecordMetadata
import com.odpm.voicejournal.domain.model.VoiceRecord
import com.odpm.voicejournal.domain.model.VoiceRecordId
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

/** Rule R3 (Default Display Name). */
class DisplayNameRuleTest {

    private fun record(title: String?) = VoiceRecord(
        id = VoiceRecordId("id"),
        audio = AudioFile("/audio/1.m4a"),
        metadata = RecordMetadata(
            title = title,
            createdAt = Instant.parse("2026-07-09T14:30:00Z"),
            updatedAt = Instant.parse("2026-07-09T14:30:00Z"),
            durationMillis = 1_000,
        ),
    )

    @Test
    fun `R3 - untitled records display Untitled Log with creation time`() {
        assertEquals(
            "Untitled Log — 2026/07/09 14:30",
            record(title = null).displayName(ZoneOffset.UTC),
        )
    }

    @Test
    fun `R3 - blank titles fall back to the default display name`() {
        assertEquals(
            "Untitled Log — 2026/07/09 14:30",
            record(title = "   ").displayName(ZoneOffset.UTC),
        )
    }

    @Test
    fun `a provided title is displayed as-is`() {
        assertEquals("Standup notes", record(title = "Standup notes").displayName(ZoneOffset.UTC))
    }

    @Test
    fun `display name respects the given time zone`() {
        assertEquals(
            "Untitled Log — 2026/07/09 23:30",
            record(title = null).displayName(ZoneOffset.ofHours(9)),
        )
    }
}
