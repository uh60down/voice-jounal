package com.odpm.voicejournal.domain

import com.odpm.voicejournal.domain.model.RecordedAudio
import com.odpm.voicejournal.domain.model.VoiceRecordId
import com.odpm.voicejournal.domain.usecase.SaveVoiceRecordUseCase
import com.odpm.voicejournal.domain.usecase.StartRecordingUseCase
import com.odpm.voicejournal.domain.usecase.StopRecordingUseCase
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/** Rules R1 (Capture First), R2 (Metadata is Optional), R4 (Audio is Required), R8 (Single Recording Session). */
class CaptureFirstRulesTest {

    private val recorder = FakeAudioRecorder()
    private val player = FakeAudioPlayer()
    private val repository = FakeVoiceRecordRepository()

    private val startRecording = StartRecordingUseCase(recorder, player)
    private val stopRecording = StopRecordingUseCase(recorder)
    private val saveVoiceRecord = SaveVoiceRecordUseCase(repository) { VoiceRecordId("fixed-id") }

    @Test
    fun `R1 and R2 - a captured thought is saved without any metadata`() = runTest {
        recorder.nextResult = RecordedAudio(
            uri = "/audio/1.m4a",
            durationMillis = 4_200,
            recordedAt = Instant.parse("2026-07-09T14:30:00Z"),
        )

        startRecording()
        val record = saveVoiceRecord(stopRecording())

        assertNull(record.metadata.title)
        assertNull(record.metadata.description)
        assertEquals(1, repository.current.size)
    }

    @Test
    fun `R4 - the saved record carries exactly the captured audio`() = runTest {
        recorder.nextResult = RecordedAudio(
            uri = "/audio/keep-me.m4a",
            durationMillis = 900,
            recordedAt = Instant.EPOCH,
        )

        startRecording()
        val record = saveVoiceRecord(stopRecording())

        assertEquals("/audio/keep-me.m4a", record.audio.uri)
        assertEquals(900, record.metadata.durationMillis)
    }

    @Test
    fun `R8 - starting a second recording session is rejected`() = runTest {
        startRecording()

        assertFailsWith<RecordingAlreadyActiveException> { startRecording() }
        assertEquals(1, recorder.startCount)
    }

    @Test
    fun `stopping without an active session is rejected`() = runTest {
        assertFailsWith<NoActiveRecordingException> { stopRecording() }
    }

    @Test
    fun `starting a recording stops any current playback`() = runTest {
        recorder.nextResult = RecordedAudio("/audio/1.m4a", 100, Instant.EPOCH)
        startRecording()
        val record = saveVoiceRecord(stopRecording())
        player.play(record)

        startRecording()

        assertTrue(player.playback.value is com.odpm.voicejournal.domain.model.PlaybackState.Idle)
    }
}
