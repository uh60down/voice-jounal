package com.odpm.voicejournal.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [VoiceRecordEntity::class], version = 1, exportSchema = false)
abstract class JournalDatabase : RoomDatabase() {
    abstract fun voiceRecordDao(): VoiceRecordDao
}
