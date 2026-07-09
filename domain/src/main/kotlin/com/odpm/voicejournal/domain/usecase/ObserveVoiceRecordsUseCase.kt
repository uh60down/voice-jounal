package com.odpm.voicejournal.domain.usecase

import com.odpm.voicejournal.domain.model.VoiceRecord
import com.odpm.voicejournal.domain.port.VoiceRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * ODPM action: View VoiceRecordList
 * (ODPM relationship: VoiceRecordList displays → VoiceRecord).
 *
 * Newest first, because the journal is a stream of thoughts and the most
 * recent capture is the most likely one to be revisited.
 */
class ObserveVoiceRecordsUseCase(
    private val repository: VoiceRecordRepository,
) {
    operator fun invoke(): Flow<List<VoiceRecord>> =
        repository.observeAll().map { records ->
            records.sortedByDescending { it.metadata.createdAt }
        }
}
