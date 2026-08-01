package com.ricordino.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricordino.data.repository.NotesRepository
import com.ricordino.pipeline.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NotesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val noteId: Long = checkNotNull(savedStateHandle[NOTE_ID_ARG]) {
        "NoteDetailViewModel requires a '$NOTE_ID_ARG' nav argument"
    }

    private val _uiState = MutableStateFlow<NoteDetailUiState>(NoteDetailUiState.Loading)
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val note = repository.getNoteById(noteId)
            _uiState.value = note?.let { NoteDetailUiState.Loaded(it) } ?: NoteDetailUiState.NotFound
        }
    }

    fun delete() {
        val state = _uiState.value
        if (state !is NoteDetailUiState.Loaded) return
        viewModelScope.launch {
            repository.deleteNote(state.note)
            _uiState.value = NoteDetailUiState.Deleted
        }
    }

    fun startEditing() {
        _uiState.update { state -> if (state is NoteDetailUiState.Loaded) state.copy(isEditing = true) else state }
    }

    fun cancelEditing() {
        _uiState.update { state ->
            if (state is NoteDetailUiState.Loaded) {
                state.copy(
                    isEditing = false,
                    editedText = state.note.extractedText,
                    editedCategory = Category.valueOf(state.note.category),
                )
            } else {
                state
            }
        }
    }

    fun updateEditedText(text: String) {
        _uiState.update { state -> if (state is NoteDetailUiState.Loaded) state.copy(editedText = text) else state }
    }

    fun updateEditedCategory(category: Category) {
        _uiState.update { state -> if (state is NoteDetailUiState.Loaded) state.copy(editedCategory = category) else state }
    }

    fun saveEdits() {
        val current = _uiState.value
        if (current !is NoteDetailUiState.Loaded || !current.isEditing || current.isSaving) return

        _uiState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            repository.updateNote(current.note, current.editedText, current.editedCategory)
            val updatedNote = current.note.copy(
                extractedText = current.editedText,
                category = current.editedCategory.name,
            )
            _uiState.value = NoteDetailUiState.Loaded(updatedNote)
        }
    }

    companion object {
        const val NOTE_ID_ARG = "noteId"
    }
}
