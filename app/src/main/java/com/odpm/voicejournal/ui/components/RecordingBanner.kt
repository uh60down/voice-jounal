package com.odpm.voicejournal.ui.components

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.odpm.voicejournal.R
import com.odpm.voicejournal.ui.formatDuration
import kotlinx.coroutines.delay

/**
 * Visible counterpart of the ODPM application state "Recording": a live
 * elapsed-time ticker and nothing else — no fields to fill in (rule R1).
 */
@Composable
fun RecordingBanner(startedAtElapsedRealtime: Long) {
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    LaunchedEffect(startedAtElapsedRealtime) {
        while (true) {
            elapsedMillis = SystemClock.elapsedRealtime() - startedAtElapsedRealtime
            delay(250)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Mic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
        Text(
            text = stringResource(R.string.recording_banner, formatDuration(elapsedMillis)),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}
