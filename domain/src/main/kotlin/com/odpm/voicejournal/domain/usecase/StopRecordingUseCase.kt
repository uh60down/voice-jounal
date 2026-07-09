package com.odpm.voicejournal.domain.usecase

import com.odpm.voicejournal.domain.NoActiveRecordingException
import com.odpm.voicejournal.domain.model.RecordedAudio
import com.odpm.voicejournal.domain.port.AudioRecorder

/**
 * ODPM action: Stop Recording.
 *
 * Ends the active recording session and hands the captured audio to
 * [SaveVoiceRecordUseCase]. Kept as a separate use case because the
 * ontology lists "Stop Recording" and "Save VoiceRecord" as distinct
 * behaviors.
 */
class StopRecordingUseCase(
    private val recorder: AudioRecorder,
) {
    suspend operator fun invoke(): RecordedAudio {
        if (!recorder.isRecording.value) throw NoActiveRecordingException()
        return recorder.stop()
    }
}
