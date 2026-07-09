package com.odpm.voicejournal.domain.usecase

import com.odpm.voicejournal.domain.RecordingAlreadyActiveException
import com.odpm.voicejournal.domain.port.AudioPlayer
import com.odpm.voicejournal.domain.port.AudioRecorder

/**
 * ODPM action: Record Voice (User creates → VoiceRecord, step 1).
 *
 * Rule R1 (Capture First): this use case takes no arguments — there is no
 * way to pass a title, description, tag, or any other metadata before
 * recording. Recording begins immediately.
 *
 * Rule R8 (Single Recording Session): starting while a session is active
 * throws [RecordingAlreadyActiveException].
 *
 * Implementation decision: any current playback is stopped first, because
 * microphone capture and speaker playback of another thought at the same
 * time would corrupt the capture — capture always wins.
 */
class StartRecordingUseCase(
    private val recorder: AudioRecorder,
    private val player: AudioPlayer,
) {
    suspend operator fun invoke() {
        if (recorder.isRecording.value) throw RecordingAlreadyActiveException()
        player.stop()
        recorder.start()
    }
}
