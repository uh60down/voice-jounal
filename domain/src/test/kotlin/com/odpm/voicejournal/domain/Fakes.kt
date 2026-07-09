package com.odpm.voicejournal.domain

import com.odpm.voicejournal.domain.model.PlaybackState
import com.odpm.voicejournal.domain.model.RecordedAudio
import com.odpm.voicejournal.domain.model.VoiceRecord
import com.odpm.voicejournal.domain.model.VoiceRecordId
import com.odpm.voicejournal.domain.port.AudioPlayer
import com.odpm.voicejournal.domain.port.AudioRecorder
import com.odpm.voicejournal.domain.port.VoiceRecordRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeVoiceRecordRepository : VoiceRecordRepository {
    private val records = MutableStateFlow<Map<VoiceRecordId, VoiceRecord>>(emptyMap())

    val current: List<VoiceRecord> get() = records.value.values.toList()

    override fun observeAll(): Flow<List<VoiceRecord>> = records.map { it.values.toList() }

    override suspend fun get(id: VoiceRecordId): VoiceRecord? = records.value[id]

    override suspend fun save(record: VoiceRecord) {
        records.value += (record.id to record)
    }

    override suspend fun updateMetadata(
        id: VoiceRecordId,
        title: String?,
        description: String?,
        updatedAt: Instant,
    ) {
        val existing = records.value.getValue(id)
        records.value += (id to existing.copy(
            metadata = existing.metadata.copy(
                title = title,
                description = description,
                updatedAt = updatedAt,
            ),
        ))
    }

    override suspend fun delete(ids: Set<VoiceRecordId>) {
        records.value -= ids
    }
}

class FakeAudioRecorder : AudioRecorder {
    private val recording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = recording.asStateFlow()

    var startCount = 0
        private set
    var nextResult: RecordedAudio =
        RecordedAudio(uri = "/fake/audio.m4a", durationMillis = 1_000, recordedAt = Instant.EPOCH)

    override suspend fun start() {
        startCount++
        recording.value = true
    }

    override suspend fun stop(): RecordedAudio {
        recording.value = false
        return nextResult
    }
}

class FakeAudioPlayer : AudioPlayer {
    private val state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val playback: StateFlow<PlaybackState> = state.asStateFlow()

    var lastPlayed: VoiceRecord? = null
        private set
    var stopCount = 0
        private set

    override suspend fun play(record: VoiceRecord) {
        lastPlayed = record
        state.value = PlaybackState.Playing(record.id)
    }

    override suspend fun stop() {
        stopCount++
        state.value = PlaybackState.Idle
    }
}
