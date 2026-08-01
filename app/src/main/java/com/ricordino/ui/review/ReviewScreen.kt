package com.ricordino.ui.review

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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

@Composable
fun ReviewScreen(
    onNoteSaved: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is ReviewUiState.Saved) onNoteSaved()
    }

    when (val state = uiState) {
        is ReviewUiState.Processing, is ReviewUiState.Saved -> ProcessingContent()
        is ReviewUiState.Error -> ErrorContent(message = state.message)
        is ReviewUiState.Editing -> EditingContent(
            state = state,
            onTextChange = viewModel::updateText,
            onCategoryChange = viewModel::updateCategory,
            onSave = viewModel::save,
        )
    }
}

@Composable
private fun ProcessingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message)
    }
}

@Composable
private fun EditingContent(
    state: ReviewUiState.Editing,
    onTextChange: (String) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onSave: () -> Unit,
) {
    val imageBitmap = remember(state.photoPath) {
        BitmapFactory.decodeFile(state.photoPath)?.asImageBitmap()
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
                contentDescription = "Captured photo",
                modifier = Modifier.fillMaxWidth().height(240.dp),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = state.extractedText,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Extracted text") },
            minLines = 4,
        )

        Spacer(modifier = Modifier.height(16.dp))

        CategoryDropdown(
            category = state.category,
            onCategoryChange = onCategoryChange,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.detectedEntities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Detected: " + state.detectedEntities.joinToString { "${it.type.name.lowercase()}: ${it.value}" })
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSave,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isSaving) "Saving…" else "Save note")
        }
    }
}
