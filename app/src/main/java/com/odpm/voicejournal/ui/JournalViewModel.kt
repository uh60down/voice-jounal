package com.odpm.voicejournal.ui

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.odpm.voicejournal.AppContainer
import com.odpm.voicejournal.domain.model.VoiceRecord
import com.odpm.voicejournal.domain.model.VoiceRecordId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Orchestrates the ODPM behaviors for the single journal screen. All domain
 * decisions live in the use cases; this class only sequences them and keeps
 * the application-state machine ([JournalMode]).
 */
class JournalViewModel(
    private val container: AppContainer,
) : ViewModel() {

    private val mode = MutableStateFlow<JournalMode>(JournalMode.Idle)
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<JournalUiState> = combine(
        container.observeVoiceRecords(),
        mode,
        container.playbackState,
        message,
    ) { records, currentMode, playback, currentMessage ->
        JournalUiState(
            records = records,
            mode = currentMode,
            playback = playback,
            message = currentMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JournalUiState())

    // --- Capture (ODPM: Record Voice / Stop Recording / Save VoiceRecord) ---

    fun startRecording() {
        viewModelScope.launch {
            runCatching { container.startRecording() }
                .onSuccess { mode.value = JournalMode.Recording(SystemClock.elapsedRealtime()) }
                .onFailure { message.value = it.message }
        }
    }

    fun stopAndSaveRecording() {
        viewModelScope.launch {
            runCatching { container.saveVoiceRecord(container.stopRecording()) }
                .onFailure { message.value = it.message }
            mode.value = JournalMode.Idle
        }
    }

    // --- Playback (ODPM: Play VoiceRecord / Stop Playback; rule R5) ---

    fun play(id: VoiceRecordId) {
        if (mode.value is JournalMode.Recording) return // the microphone owns the session
        viewModelScope.launch {
            runCatching { container.playVoiceRecord(id) }
                .onFailure { message.value = it.message }
        }
    }

    fun stopPlayback() {
        viewModelScope.launch { container.stopPlayback() }
    }

    // --- Selection Mode (ODPM: Select VoiceRecord; rule R7) ---

    fun enterSelection(id: VoiceRecordId) {
        if (mode.value is JournalMode.Idle) {
            mode.value = JournalMode.Selection(selected = setOf(id))
        }
    }

    fun toggleSelection(id: VoiceRecordId) {
        val current = mode.value as? JournalMode.Selection ?: return
        val selected = if (id in current.selected) current.selected - id else current.selected + id
        mode.value = if (selected.isEmpty()) JournalMode.Idle else current.copy(selected = selected)
    }

    fun exitSelection() {
        if (mode.value is JournalMode.Selection) mode.value = JournalMode.Idle
    }

    fun requestDeleteSelected() {
        val current = mode.value as? JournalMode.Selection ?: return
        if (current.selected.isNotEmpty()) mode.value = current.copy(confirmingDelete = true)
    }

    fun cancelDelete() {
        val current = mode.value as? JournalMode.Selection ?: return
        mode.value = current.copy(confirmingDelete = false)
    }

    fun confirmDeleteSelected() {
        val current = mode.value as? JournalMode.Selection ?: return
        viewModelScope.launch {
            runCatching { container.deleteVoiceRecords(current.selected) }
                .onFailure { message.value = it.message }
            mode.value = JournalMode.Idle
        }
    }

    // --- Metadata Editing (ODPM: Update Metadata; rules R2, R6) ---

    fun startEditing(record: VoiceRecord) {
        if (mode.value is JournalMode.Idle) {
            mode.value = JournalMode.MetadataEditing(record)
        }
    }

    fun saveMetadata(title: String, description: String) {
        val current = mode.value as? JournalMode.MetadataEditing ?: return
        viewModelScope.launch {
            runCatching { container.updateMetadata(current.record.id, title, description) }
                .onFailure { message.value = it.message }
            mode.value = JournalMode.Idle
        }
    }

    fun dismissEditing() {
        if (mode.value is JournalMode.MetadataEditing) mode.value = JournalMode.Idle
    }

    fun onPermissionDenied(text: String) {
        message.value = text
    }

    fun consumeMessage() {
        message.value = null
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    JournalViewModel(container) as T
            }
    }
}
