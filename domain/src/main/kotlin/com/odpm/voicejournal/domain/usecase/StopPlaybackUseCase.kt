package com.odpm.voicejournal.domain.usecase

import com.odpm.voicejournal.domain.port.AudioPlayer

/** ODPM action: Stop Playback (User stops → VoiceRecord). */
class StopPlaybackUseCase(
    private val player: AudioPlayer,
) {
    suspend operator fun invoke() {
        player.stop()
    }
}
