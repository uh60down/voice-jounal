package com.odpm.voicejournal.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.odpm.voicejournal.R
import com.odpm.voicejournal.VoiceJournalApp
import com.odpm.voicejournal.ui.theme.VoiceJournalTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as VoiceJournalApp).container
        setContent {
            VoiceJournalTheme {
                JournalRoute(
                    viewModel = viewModel(factory = JournalViewModel.factory(container)),
                )
            }
        }
    }
}

@Composable
private fun JournalRoute(viewModel: JournalViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionDeniedText = stringResource(R.string.permission_denied)

    // R1 (Capture First): the microphone permission is the only gate between
    // the user and capture, asked on the very first record tap and never
    // accompanied by any metadata prompt.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.startRecording() else viewModel.onPermissionDenied(permissionDeniedText)
    }

    JournalScreen(
        uiState = uiState,
        onRecordClick = {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) viewModel.startRecording() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        },
        onStopRecordingClick = viewModel::stopAndSaveRecording,
        onPlay = viewModel::play,
        onStopPlayback = viewModel::stopPlayback,
        onRecordLongPress = viewModel::enterSelection,
        onToggleSelection = viewModel::toggleSelection,
        onExitSelection = viewModel::exitSelection,
        onRequestDelete = viewModel::requestDeleteSelected,
        onConfirmDelete = viewModel::confirmDeleteSelected,
        onCancelDelete = viewModel::cancelDelete,
        onEdit = viewModel::startEditing,
        onSaveMetadata = viewModel::saveMetadata,
        onDismissEditing = viewModel::dismissEditing,
        onMessageShown = viewModel::consumeMessage,
    )
}
