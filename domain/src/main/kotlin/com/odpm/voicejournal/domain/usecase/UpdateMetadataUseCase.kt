package com.odpm.voicejournal.domain.usecase

import com.odpm.voicejournal.domain.VoiceRecordNotFoundException
import com.odpm.voicejournal.domain.model.VoiceRecordId
import com.odpm.voicejournal.domain.port.VoiceRecordRepository
import java.time.Clock

/**
 * ODPM action: Update Metadata (User updates → VoiceRecord).
 *
 * Rule R6 (Metadata Editing): only title and description can change — the
 * signature offers nothing else, and the audio is untouched by design.
 * Rule R2 (Metadata is Optional): blank input is normalized to null, so
 * clearing a title returns the record to its untitled state and rule R3's
 * default display name takes over again.
 */
class UpdateMetadataUseCase(
    private val repository: VoiceRecordRepository,
    private val clock: Clock = Clock.systemUTC(),
) {
    suspend operator fun invoke(id: VoiceRecordId, title: String?, description: String?) {
        repository.get(id) ?: throw VoiceRecordNotFoundException(id)
        repository.updateMetadata(
            id = id,
            title = title?.trim()?.takeIf { it.isNotEmpty() },
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            updatedAt = clock.instant(),
        )
    }
}
