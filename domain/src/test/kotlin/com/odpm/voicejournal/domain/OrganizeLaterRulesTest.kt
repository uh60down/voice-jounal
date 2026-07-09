package com.odpm.voicejournal.domain

import com.odpm.voicejournal.domain.model.AudioFile
import com.odpm.voicejournal.domain.model.PlaybackState
import com.odpm.voicejournal.domain.model.RecordMetadata
import com.odpm.voicejournal.domain.model.VoiceRecord
import com.odpm.voicejournal.domain.model.VoiceRecordId
import com.odpm.voicejournal.domain.usecase.DeleteVoiceRecordsUseCase
import com.odpm.voicejournal.domain.usecase.UpdateMetadataUseCase
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

/** Rules R6 (Metadata Editing) and R7 (Deletion). */
class OrganizeLaterRulesTest {

    private val repository = FakeVoiceRecordRepository()
    private val player = FakeAudioPlayer()
    private val now = Instant.parse("2026-07-09T15:00:00Z")
    private val updateMetadata = UpdateMetadataUseCase(repository, Clock.fixed(now, ZoneOffset.UTC))
    private val deleteVoiceRecords = DeleteVoiceRecordsUseCase(repository, player)

    private fun savedRecord(id: String) = VoiceRecord(
        id = VoiceRecordId(id),
        audio = AudioFile("/audio/$id.m4a"),
        metadata = RecordMetadata(
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            durationMillis = 1_000,
        ),
    )

    @Test
    fun `R6 - updating metadata changes only title, description and updatedAt`() = runTest {
        val record = savedRecord("a")
        repository.save(record)

        updateMetadata(record.id, title = "My idea", description = "Details")

        val updated = repository.get(record.id)!!
        assertEquals("My idea", updated.metadata.title)
        assertEquals("Details", updated.metadata.description)
        assertEquals(now, updated.metadata.updatedAt)
        // The audio and creation time are untouched (R6: audio is immutable).
        assertEquals(record.audio, updated.audio)
        assertEquals(record.metadata.createdAt, updated.metadata.createdAt)
        assertEquals(record.metadata.durationMillis, updated.metadata.durationMillis)
    }

    @Test
    fun `R6 and R2 - clearing the title makes the record untitled again`() = runTest {
        val record = savedRecord("a")
        repository.save(record)
        updateMetadata(record.id, title = "My idea", description = null)

        updateMetadata(record.id, title = "   ", description = "")

        val updated = repository.get(record.id)!!
        assertNull(updated.metadata.title)
        assertNull(updated.metadata.description)
    }

    @Test
    fun `R6 - editing a record that does not exist is rejected`() = runTest {
        assertFailsWith<VoiceRecordNotFoundException> {
            updateMetadata(VoiceRecordId("missing"), title = "x", description = null)
        }
    }

    @Test
    fun `R7 - multiple records can be deleted at once`() = runTest {
        val a = savedRecord("a")
        val b = savedRecord("b")
        val c = savedRecord("c")
        repository.save(a)
        repository.save(b)
        repository.save(c)

        deleteVoiceRecords(setOf(a.id, c.id))

        assertEquals(listOf(b), repository.current)
    }

    @Test
    fun `R7 - deleting the record that is playing stops playback first`() = runTest {
        val record = savedRecord("a")
        repository.save(record)
        player.play(record)

        deleteVoiceRecords(setOf(record.id))

        assertEquals(PlaybackState.Idle, player.playback.value)
        assertEquals(emptyList(), repository.current)
    }

    @Test
    fun `R7 - deleting other records leaves current playback alone`() = runTest {
        val playing = savedRecord("playing")
        val doomed = savedRecord("doomed")
        repository.save(playing)
        repository.save(doomed)
        player.play(playing)

        deleteVoiceRecords(setOf(doomed.id))

        assertEquals(PlaybackState.Playing(playing.id), player.playback.value)
    }
}
