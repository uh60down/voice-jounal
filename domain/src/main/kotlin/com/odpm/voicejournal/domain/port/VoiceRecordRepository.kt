package com.odpm.voicejournal.domain.port

import com.odpm.voicejournal.domain.model.VoiceRecord
import com.odpm.voicejournal.domain.model.VoiceRecordId
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/**
 * Persistence port for VoiceRecords. Implemented in the app layer
 * (Room + files); the domain only depends on this contract.
 *
 * The narrow surface encodes the rules:
 *  - [updateMetadata] can only touch title/description/updatedAt (rule R6 —
 *    the audio is immutable because no operation here can change it).
 *  - [delete] accepts a set of ids (rule R7 — delete one or multiple).
 */
interface VoiceRecordRepository {
    /** ODPM relationship: VoiceRecordList displays → VoiceRecord. */
    fun observeAll(): Flow<List<VoiceRecord>>

    suspend fun get(id: VoiceRecordId): VoiceRecord?

    suspend fun save(record: VoiceRecord)

    suspend fun updateMetadata(
        id: VoiceRecordId,
        title: String?,
        description: String?,
        updatedAt: Instant,
    )

    suspend fun delete(ids: Set<VoiceRecordId>)
}
