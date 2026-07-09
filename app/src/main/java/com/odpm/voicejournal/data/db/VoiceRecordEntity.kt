package com.odpm.voicejournal.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.odpm.voicejournal.domain.model.AudioFile
import com.odpm.voicejournal.domain.model.RecordMetadata
import com.odpm.voicejournal.domain.model.VoiceRecord
import com.odpm.voicejournal.domain.model.VoiceRecordId
import java.time.Instant

/**
 * Persistence shape of a VoiceRecord. The database flattens the ontology's
 * VoiceRecord → AudioFile / RecordMetadata composition into one row for
 * simplicity; the mapping functions below restore the domain structure, so
 * the rest of the app only ever sees the ontology's concepts.
 */
@Entity(tableName = "voice_records")
data class VoiceRecordEntity(
    @PrimaryKey val id: String,
    val audioUri: String,
    val title: String?,
    val description: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val durationMillis: Long,
)

fun VoiceRecordEntity.toDomain(): VoiceRecord = VoiceRecord(
    id = VoiceRecordId(id),
    audio = AudioFile(uri = audioUri),
    metadata = RecordMetadata(
        title = title,
        description = description,
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
        updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
        durationMillis = durationMillis,
    ),
)

fun VoiceRecord.toEntity(): VoiceRecordEntity = VoiceRecordEntity(
    id = id.value,
    audioUri = audio.uri,
    title = metadata.title,
    description = metadata.description,
    createdAtEpochMillis = metadata.createdAt.toEpochMilli(),
    updatedAtEpochMillis = metadata.updatedAt.toEpochMilli(),
    durationMillis = metadata.durationMillis,
)
