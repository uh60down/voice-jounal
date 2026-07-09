package com.odpm.voicejournal.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.odpm.voicejournal.R
import com.odpm.voicejournal.domain.model.VoiceRecord

/**
 * ODPM application state: Metadata Editing (action: Update Metadata).
 *
 * Rule R6: only title and description are editable — the audio is not even
 * represented here. Rule R2: both fields may be left (or made) empty; the
 * domain normalizes blanks to null, and rule R3's default display name
 * takes over for untitled records.
 */
@Composable
fun MetadataEditDialog(
    record: VoiceRecord,
    onSave: (title: String, description: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember(record.id) { mutableStateOf(record.metadata.title.orEmpty()) }
    var description by remember(record.id) { mutableStateOf(record.metadata.description.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.field_title)) },
                    placeholder = { Text(record.displayName()) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.field_description)) },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, description) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
