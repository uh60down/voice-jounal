package com.odpm.voicejournal.domain.model

/** Identity of a [VoiceRecord], independent of any storage technology. */
@JvmInline
value class VoiceRecordId(val value: String) {
    init {
        require(value.isNotBlank()) { "VoiceRecordId must not be blank" }
    }
}
