package com.odpm.voicejournal.domain.usecase

import com.odpm.voicejournal.domain.model.AudioFile
import com.odpm.voicejournal.domain.model.RecordMetadata
import com.odpm.voicejournal.domain.model.RecordedAudio
import com.odpm.voicejournal.domain.model.VoiceRecord
import com.odpm.voicejournal.domain.model.VoiceRecordId
import com.odpm.voicejournal.domain.port.VoiceRecordRepository
import java.util.UUID

/**
 * ODPM action: Save VoiceRecord (User creates → VoiceRecord, step 2).
 *
 * Rule R1/R2: the record is saved with no title and no description —
 * organizing happens later, if ever.
 * Rule R4: the AudioFile is constructed here and is required; a
 * VoiceRecord without audio cannot be represented.
 */
class SaveVoiceRecordUseCase(
    private val repository: VoiceRecordRepository,
    private val newId: () -> VoiceRecordId = { VoiceRecordId(UUID.randomUUID().toString()) },
) {
    suspend operator fun invoke(audio: RecordedAudio): VoiceRecord {
        val record = VoiceRecord(
            id = newId(),
            audio = AudioFile(uri = audio.uri),
            metadata = RecordMetadata(
                title = null,
                description = null,
                createdAt = audio.recordedAt,
                updatedAt = audio.recordedAt,
                durationMillis = audio.durationMillis,
            ),
        )
        repository.save(record)
        return record
    }
}
