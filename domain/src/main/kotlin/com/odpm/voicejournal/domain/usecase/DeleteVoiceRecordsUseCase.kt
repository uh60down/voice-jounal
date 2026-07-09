package com.odpm.voicejournal.domain.usecase

import com.odpm.voicejournal.domain.model.PlaybackState
import com.odpm.voicejournal.domain.model.VoiceRecordId
import com.odpm.voicejournal.domain.port.AudioPlayer
import com.odpm.voicejournal.domain.port.VoiceRecordRepository

/**
 * ODPM action: Delete VoiceRecord(s) (User deletes → VoiceRecord).
 *
 * Rule R7 (Deletion): accepts a set of ids so one or multiple records can
 * be deleted in a single behavior.
 *
 * A record that is currently playing stops first — a deleted record has no
 * states left, including Playing.
 */
class DeleteVoiceRecordsUseCase(
    private val repository: VoiceRecordRepository,
    private val player: AudioPlayer,
) {
    suspend operator fun invoke(ids: Set<VoiceRecordId>) {
        if (ids.isEmpty()) return
        val playback = player.playback.value
        if (playback is PlaybackState.Playing && playback.recordId in ids) {
            player.stop()
        }
        repository.delete(ids)
    }
}
