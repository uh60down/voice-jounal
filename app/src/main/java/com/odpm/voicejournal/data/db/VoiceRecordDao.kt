package com.odpm.voicejournal.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceRecordDao {

    @Query("SELECT * FROM voice_records ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<VoiceRecordEntity>>

    @Query("SELECT * FROM voice_records WHERE id = :id")
    suspend fun getById(id: String): VoiceRecordEntity?

    @Query("SELECT * FROM voice_records WHERE id IN (:ids)")
    suspend fun getByIds(ids: Set<String>): List<VoiceRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: VoiceRecordEntity)

    // R6: the only UPDATE in the app touches title/description/updatedAt.
    @Query(
        "UPDATE voice_records SET title = :title, description = :description, " +
            "updatedAtEpochMillis = :updatedAtEpochMillis WHERE id = :id",
    )
    suspend fun updateMetadata(id: String, title: String?, description: String?, updatedAtEpochMillis: Long)

    @Query("DELETE FROM voice_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: Set<String>)
}
