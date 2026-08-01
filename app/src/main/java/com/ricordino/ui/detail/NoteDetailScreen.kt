package com.ricordino.ui.detail

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricordino.pipeline.Category
import com.ricordino.ui.components.CategoryDropdown
import java.text.DateFormat
import java.util.Date

@Composable
fun NoteDetailScreen(
    onDeleted: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is NoteDetailUiState.Deleted) onDeleted()
    }

    when (val state = uiState) {
        is NoteDetailUiState.Loading, is NoteDetailUiState.Deleted -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }

        is NoteDetailUiState.NotFound -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) { Text("This note no longer exists.") }

        is NoteDetailUiState.Loaded -> LoadedContent(
            state = state,
            onDelete = viewModel::delete,
            onStartEditing = viewModel::startEditing,
            onCancelEditing = viewModel::cancelEditing,
            onTextChange = viewModel::updateEditedText,
            onCategoryChange = viewModel::updateEditedCategory,
            onSaveEdits = viewModel::saveEdits,
        )
    }
}

@Composable
private fun LoadedContent(
    state: NoteDetailUiState.Loaded,
    onDelete: () -> Unit,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onTextChange: (String) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onSaveEdits: () -> Unit,
) {
    val note = state.note
    val imageBitmap = remember(note.imageFilePath) {
        BitmapFactory.decodeFile(note.imageFilePath)?.asImageBitmap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(16.dp),
    ) {
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Note photo",
                modifier = Modifier.fillMaxWidth().height(280.dp),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = DateFormat.getDateTimeInstance().format(Date(note.timestamp)),
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (state.isEditing) {
            OutlinedTextField(
                value = state.editedText,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Extracted text") },
                minLines = 4,
            )
            Spacer(modifier = Modifier.height(16.dp))
            CategoryDropdown(
                category = state.editedCategory,
                onCategoryChange = onCategoryChange,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(note.category, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(note.extractedText)
        }

        if (note.detectedEntities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Detected: " + note.detectedEntities.joinToString { "${it.type.name.lowercase()}: ${it.value}" })
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isEditing) {
            Row {
                OutlinedButton(onClick = onCancelEditing, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onSaveEdits,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (state.isSaving) "Saving…" else "Save changes")
                }
            }
        } else {
            Row {
                OutlinedButton(onClick = onStartEditing, modifier = Modifier.weight(1f)) {
                    Text("Edit")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Text("Delete note")
                }
            }
        }
    }
}
