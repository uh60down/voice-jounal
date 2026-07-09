package com.odpm.voicejournal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.odpm.voicejournal.R
import com.odpm.voicejournal.domain.model.PlaybackState
import com.odpm.voicejournal.domain.model.VoiceRecord
import com.odpm.voicejournal.domain.model.VoiceRecordId
import com.odpm.voicejournal.ui.components.MetadataEditDialog
import com.odpm.voicejournal.ui.components.RecordingBanner
import com.odpm.voicejournal.ui.components.VoiceRecordRow

/**
 * The single screen of the MVP. Its regions map to the ontology:
 *  - the list is the VoiceRecordList (displays → VoiceRecord),
 *  - the FAB is the capture behavior (Record Voice / Stop Recording),
 *  - the top bar switches with the application state (Idle vs Selection Mode),
 *  - the dialogs are the Metadata Editing state and the R7 delete confirmation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    uiState: JournalUiState,
    onRecordClick: () -> Unit,
    onStopRecordingClick: () -> Unit,
    onPlay: (VoiceRecordId) -> Unit,
    onStopPlayback: () -> Unit,
    onRecordLongPress: (VoiceRecordId) -> Unit,
    onToggleSelection: (VoiceRecordId) -> Unit,
    onExitSelection: () -> Unit,
    onRequestDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onEdit: (VoiceRecord) -> Unit,
    onSaveMetadata: (title: String, description: String) -> Unit,
    onDismissEditing: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    val selection = uiState.mode as? JournalMode.Selection
    val isRecording = uiState.mode is JournalMode.Recording

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (selection != null) {
                TopAppBar(
                    title = { Text(stringResource(R.string.selection_count, selection.selected.size)) },
                    navigationIcon = {
                        IconButton(onClick = onExitSelection) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.exit_selection))
                        }
                    },
                    actions = {
                        IconButton(onClick = onRequestDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_selected))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                )
            } else {
                TopAppBar(title = { Text(stringResource(R.string.app_name)) })
            }
        },
        floatingActionButton = {
            // ODPM actions: Record Voice / Stop Recording + Save VoiceRecord.
            FloatingActionButton(
                onClick = if (isRecording) onStopRecordingClick else onRecordClick,
                containerColor = if (isRecording) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                    contentDescription = stringResource(
                        if (isRecording) R.string.record_stop_save else R.string.record_start,
                    ),
                )
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            (uiState.mode as? JournalMode.Recording)?.let { recording ->
                RecordingBanner(startedAtElapsedRealtime = recording.startedAtElapsedRealtime)
            }

            if (uiState.records.isEmpty() && !isRecording) {
                EmptyState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 96.dp),
                ) {
                    items(uiState.records, key = { it.id.value }) { record ->
                        val playing = (uiState.playback as? PlaybackState.Playing)?.recordId == record.id
                        VoiceRecordRow(
                            record = record,
                            isPlaying = playing,
                            selectionMode = selection != null,
                            isSelected = selection?.selected?.contains(record.id) == true,
                            playEnabled = !isRecording,
                            onClick = {
                                if (selection != null) onToggleSelection(record.id)
                                else if (playing) onStopPlayback() else onPlay(record.id)
                            },
                            onLongClick = {
                                if (selection == null) onRecordLongPress(record.id)
                                else onToggleSelection(record.id)
                            },
                            onPlayStopClick = { if (playing) onStopPlayback() else onPlay(record.id) },
                            onEditClick = { onEdit(record) },
                        )
                    }
                }
            }
        }
    }

    if (selection?.confirmingDelete == true) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = {
                Text(
                    pluralStringResource(
                        R.plurals.delete_confirm_body,
                        selection.selected.size,
                        selection.selected.size,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    (uiState.mode as? JournalMode.MetadataEditing)?.let { editing ->
        MetadataEditDialog(
            record = editing.record,
            onSave = onSaveMetadata,
            onDismiss = onDismissEditing,
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.empty_state_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.empty_state_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
