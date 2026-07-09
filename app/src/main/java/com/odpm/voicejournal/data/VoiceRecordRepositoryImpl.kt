package com.odpm.voicejournal.data

import com.odpm.voicejournal.data.db.VoiceRecordDao
import com.odpm.voicejournal.data.db.toDomain
import com.odpm.voicejournal.data.db.toEntity
import com.odpm.voicejournal.domain.model.VoiceRecord
import com.odpm.voicejournal.domain.model.VoiceRecordId
import com.odpm.voicejournal.domain.port.VoiceRecordRepository
import java.io.File
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Room-backed implementation of the domain's persistence port.
 *
 * Deleting a VoiceRecord also deletes its AudioFile from disk: the ontology
 * says a VoiceRecord *contains* its AudioFile (composition, R4), so the
 * audio has no life of its own once the record is gone.
 */
class VoiceRecordRepositoryImpl(
    private val dao: VoiceRecordDao,
) : VoiceRecordRepository {

    override fun observeAll(): Flow<List<VoiceRecord>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun get(id: VoiceRecordId): VoiceRecord? =
        dao.getById(id.value)?.toDomain()

    override suspend fun save(record: VoiceRecord) {
        dao.insert(record.toEntity())
    }

    override suspend fun updateMetadata(
        id: VoiceRecordId,
        title: String?,
        description: String?,
        updatedAt: Instant,
    ) {
        dao.updateMetadata(id.value, title, description, updatedAt.toEpochMilli())
    }

    override suspend fun delete(ids: Set<VoiceRecordId>) {
        val rawIds = ids.mapTo(mutableSetOf()) { it.value }
        val doomed = dao.getByIds(rawIds)
        dao.deleteByIds(rawIds)
        withContext(Dispatchers.IO) {
            doomed.forEach { entity ->
                runCatching { File(entity.audioUri).delete() }
            }
        }
    }
}
