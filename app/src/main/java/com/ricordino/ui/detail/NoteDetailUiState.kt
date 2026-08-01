package com.ricordino.ui.detail

import com.ricordino.data.local.NoteEntity
import com.ricordino.pipeline.Category

sealed interface NoteDetailUiState {
    data object Loading : NoteDetailUiState

    data class Loaded(
        val note: NoteEntity,
        val isEditing: Boolean = false,
        val isSaving: Boolean = false,
        val editedText: String = note.extractedText,
        val editedCategory: Category = Category.valueOf(note.category),
    ) : NoteDetailUiState

    data object Deleted : NoteDetailUiState
    data object NotFound : NoteDetailUiState
}
