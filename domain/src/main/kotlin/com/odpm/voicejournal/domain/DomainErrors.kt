package com.odpm.voicejournal.domain

import com.odpm.voicejournal.domain.model.VoiceRecordId

/** Rule R8: only one recording session may be active at any time. */
class RecordingAlreadyActiveException :
    IllegalStateException("R8: only one recording session may be active at a time")

/** Stop Recording was requested while no recording session was active. */
class NoActiveRecordingException :
    IllegalStateException("No recording session is active")

/** Rule R5: only saved VoiceRecords may be played (or edited/deleted). */
class VoiceRecordNotFoundException(id: VoiceRecordId) :
    IllegalArgumentException("No saved VoiceRecord with id ${id.value}")
