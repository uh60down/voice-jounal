package com.odpm.voicejournal.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.odpm.voicejournal.domain.NoActiveRecordingException
import com.odpm.voicejournal.domain.model.RecordedAudio
import com.odpm.voicejournal.domain.port.AudioRecorder
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Microphone adapter for the domain's AudioRecorder port, built on
 * [MediaRecorder]. Audio is written as AAC/MP4 into app-private storage
 * (no storage permission needed — nothing may slow down capture, R1).
 *
 * Holding at most one [MediaRecorder] instance is the physical counterpart
 * of rule R8 (Single Recording Session); the domain use case enforces the
 * rule, this class simply cannot represent two sessions.
 */
class MediaRecorderAudioRecorder(
    private val context: Context,
) : AudioRecorder {

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val mutex = Mutex()
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAt: Instant? = null

    override suspend fun start(): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            check(recorder == null) { "Recorder already active" }

            val dir = File(context.filesDir, RECORDINGS_DIR).apply { mkdirs() }
            val file = File(dir, "${UUID.randomUUID()}.m4a")

            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            try {
                mediaRecorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(128_000)
                    setAudioSamplingRate(44_100)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                mediaRecorder.release()
                file.delete()
                throw e
            }

            recorder = mediaRecorder
            outputFile = file
            startedAt = Instant.now()
            _isRecording.value = true
        }
    }

    override suspend fun stop(): RecordedAudio = withContext(Dispatchers.IO) {
        mutex.withLock {
            val mediaRecorder = recorder ?: throw NoActiveRecordingException()
            val file = checkNotNull(outputFile)
            val recordedAt = checkNotNull(startedAt)

            recorder = null
            outputFile = null
            startedAt = null
            _isRecording.value = false

            try {
                mediaRecorder.stop()
            } catch (e: RuntimeException) {
                // stop() throws if nothing valid was captured (e.g. an
                // immediate stop). There is no thought to keep — clean up.
                mediaRecorder.release()
                file.delete()
                throw NoActiveRecordingException()
            }
            mediaRecorder.release()

            RecordedAudio(
                uri = file.absolutePath,
                durationMillis = Duration.between(recordedAt, Instant.now()).toMillis(),
                recordedAt = recordedAt,
            )
        }
    }

    private companion object {
        const val RECORDINGS_DIR = "recordings"
    }
}
