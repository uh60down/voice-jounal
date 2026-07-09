package com.odpm.voicejournal.domain.usecase

import com.odpm.voicejournal.domain.VoiceRecordNotFoundException
import com.odpm.voicejournal.domain.model.VoiceRecordId
import com.odpm.voicejournal.domain.port.AudioPlayer
import com.odpm.voicejournal.domain.port.VoiceRecordRepository

/**
 * ODPM action: Play VoiceRecord (User selects/plays → VoiceRecord).
 *
 * Rule R5 (Playback): only saved VoiceRecords may be played. The record is
 * looked up in the repository first; anything not found there is not a
 * saved record and cannot reach the player.
 */
class PlayVoiceRecordUseCase(
    private val repository: VoiceRecordRepository,
    private val player: AudioPlayer,
) {
    suspend operator fun invoke(id: VoiceRecordId) {
        val record = repository.get(id) ?: throw VoiceRecordNotFoundException(id)
        player.play(record)
    }
}
