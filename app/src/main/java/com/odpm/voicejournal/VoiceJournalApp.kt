package com.odpm.voicejournal

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.odpm.voicejournal.data.VoiceRecordRepositoryImpl
import com.odpm.voicejournal.data.audio.MediaPlayerAudioPlayer
import com.odpm.voicejournal.data.audio.MediaRecorderAudioRecorder
import com.odpm.voicejournal.data.db.JournalDatabase
import com.odpm.voicejournal.domain.port.AudioPlayer
import com.odpm.voicejournal.domain.port.AudioRecorder
import com.odpm.voicejournal.domain.port.VoiceRecordRepository
import com.odpm.voicejournal.domain.usecase.DeleteVoiceRecordsUseCase
import com.odpm.voicejournal.domain.usecase.ObserveVoiceRecordsUseCase
import com.odpm.voicejournal.domain.usecase.PlayVoiceRecordUseCase
import com.odpm.voicejournal.domain.usecase.SaveVoiceRecordUseCase
import com.odpm.voicejournal.domain.usecase.StartRecordingUseCase
import com.odpm.voicejournal.domain.usecase.StopPlaybackUseCase
import com.odpm.voicejournal.domain.usecase.StopRecordingUseCase
import com.odpm.voicejournal.domain.usecase.UpdateMetadataUseCase

class VoiceJournalApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/**
 * Manual dependency wiring — deliberately small, so the composition of the
 * app reads like the ontology: ports on top, one use case per ODPM action.
 */
class AppContainer(context: Context) {

    private val database: JournalDatabase = Room.databaseBuilder(
        context,
        JournalDatabase::class.java,
        "voice-journal.db",
    ).build()

    private val repository: VoiceRecordRepository = VoiceRecordRepositoryImpl(database.voiceRecordDao())
    private val recorder: AudioRecorder = MediaRecorderAudioRecorder(context)
    private val player: AudioPlayer = MediaPlayerAudioPlayer()

    // ODPM actions → use cases
    val startRecording = StartRecordingUseCase(recorder, player)
    val stopRecording = StopRecordingUseCase(recorder)
    val saveVoiceRecord = SaveVoiceRecordUseCase(repository)
    val observeVoiceRecords = ObserveVoiceRecordsUseCase(repository)
    val playVoiceRecord = PlayVoiceRecordUseCase(repository, player)
    val stopPlayback = StopPlaybackUseCase(player)
    val updateMetadata = UpdateMetadataUseCase(repository)
    val deleteVoiceRecords = DeleteVoiceRecordsUseCase(repository, player)

    // ODPM states, observed by the UI
    val isRecording = recorder.isRecording
    val playbackState = player.playback
}
