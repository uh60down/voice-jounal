package com.odpm.voicejournal.domain

import com.odpm.voicejournal.domain.model.AudioFile
import com.odpm.voicejournal.domain.model.PlaybackState
import com.odpm.voicejournal.domain.model.RecordMetadata
import com.odpm.voicejournal.domain.model.VoiceRecord
import com.odpm.voicejournal.domain.model.VoiceRecordId
import com.odpm.voicejournal.domain.usecase.PlayVoiceRecordUseCase
import com.odpm.voicejournal.domain.usecase.StopPlaybackUseCase
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

/** Rule R5 (Playback: only saved VoiceRecords may be played). */
class PlaybackRulesTest {

    private val repository = FakeVoiceRecordRepository()
    private val player = FakeAudioPlayer()
    private val playVoiceRecord = PlayVoiceRecordUseCase(repository, player)
    private val stopPlayback = StopPlaybackUseCase(player)

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
    fun `R5 - a saved record can be played`() = runTest {
        val record = savedRecord("a")
        repository.save(record)

        playVoiceRecord(record.id)

        assertEquals(record, player.lastPlayed)
        assertEquals(PlaybackState.Playing(record.id), player.playback.value)
    }

    @Test
    fun `R5 - a record that was never saved cannot be played`() = runTest {
        assertFailsWith<VoiceRecordNotFoundException> {
            playVoiceRecord(VoiceRecordId("never-saved"))
        }
        assertEquals(null, player.lastPlayed)
    }

    @Test
    fun `stop playback returns to idle`() = runTest {
        val record = savedRecord("a")
        repository.save(record)
        playVoiceRecord(record.id)

        stopPlayback()

        assertEquals(PlaybackState.Idle, player.playback.value)
    }
}
