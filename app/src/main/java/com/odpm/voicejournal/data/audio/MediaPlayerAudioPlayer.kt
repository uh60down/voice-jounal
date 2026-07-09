package com.odpm.voicejournal.data.audio

import android.media.MediaPlayer
import com.odpm.voicejournal.domain.model.PlaybackState
import com.odpm.voicejournal.domain.model.VoiceRecord
import com.odpm.voicejournal.domain.port.AudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Speaker adapter for the domain's AudioPlayer port, built on [MediaPlayer].
 *
 * One player instance at a time: playing a record replaces whatever was
 * playing, and natural completion returns the state to Idle so the UI's
 * play/stop affordances always reflect the ontology's playback states.
 */
class MediaPlayerAudioPlayer : AudioPlayer {

    private val _playback = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val playback: StateFlow<PlaybackState> = _playback.asStateFlow()

    private var player: MediaPlayer? = null

    override suspend fun play(record: VoiceRecord): Unit = withContext(Dispatchers.Main) {
        release()
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(record.audio.uri)
            mediaPlayer.setOnCompletionListener {
                release()
                _playback.value = PlaybackState.Idle
            }
            withContext(Dispatchers.IO) { mediaPlayer.prepare() }
            mediaPlayer.start()
        } catch (e: Exception) {
            mediaPlayer.release()
            _playback.value = PlaybackState.Idle
            throw e
        }
        player = mediaPlayer
        _playback.value = PlaybackState.Playing(record.id)
    }

    override suspend fun stop(): Unit = withContext(Dispatchers.Main) {
        release()
        _playback.value = PlaybackState.Idle
    }

    private fun release() {
        player?.let { current ->
            runCatching {
                if (current.isPlaying) current.stop()
            }
            current.release()
        }
        player = null
    }
}
