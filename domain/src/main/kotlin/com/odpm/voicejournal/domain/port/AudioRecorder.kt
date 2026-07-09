package com.odpm.voicejournal.domain.port

import com.odpm.voicejournal.domain.model.RecordedAudio
import kotlinx.coroutines.flow.StateFlow

/**
 * Recording port. Implemented in the app layer with the device microphone.
 *
 * Exposes at most one recording session at a time; the domain enforces
 * rule R8 (Single Recording Session) on top of [isRecording] in
 * [com.odpm.voicejournal.domain.usecase.StartRecordingUseCase].
 */
interface AudioRecorder {
    /** ODPM application state "Recording". */
    val isRecording: StateFlow<Boolean>

    /**
     * ODPM action: Record Voice. Begins capturing immediately —
     * no metadata is required or even accepted here (rule R1).
     */
    suspend fun start()

    /** ODPM action: Stop Recording. Returns the captured audio. */
    suspend fun stop(): RecordedAudio
}
