package com.odpm.voicejournal.domain.model

/**
 * ODPM concept: AudioFile — the recorded audio associated with a VoiceRecord.
 *
 * Rule R4 (Audio is Required) is enforced structurally: [VoiceRecord.audio]
 * is a non-nullable field of this type, so a VoiceRecord cannot exist
 * without exactly one AudioFile.
 *
 * Rule R6 (audio is immutable in the MVP) is enforced by there being no
 * behavior anywhere in the domain that replaces or edits an AudioFile.
 */
data class AudioFile(val uri: String) {
    init {
        require(uri.isNotBlank()) { "R4: an AudioFile must reference actual audio" }
    }
}
