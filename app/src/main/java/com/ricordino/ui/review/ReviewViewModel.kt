package com.ricordino.ui.review

import android.graphics.BitmapFactory
import android.net.Uri
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
class ReviewViewModel @Inject constructor(
    private val repository: NotesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val photoPath: String = Uri.decode(
        checkNotNull(savedStateHandle[PHOTO_PATH_ARG]) {
            "ReviewViewModel requires a '$PHOTO_PATH_ARG' nav argument"
        },
    )

    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Processing)
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        processPhoto()
    }

    private fun processPhoto() {
        viewModelScope.launch {
            try {
                val bitmap = BitmapFactory.decodeFile(photoPath)
                    ?: error("Couldn't read the captured photo.")
                val result = repository.processCapture(bitmap)
                _uiState.value = ReviewUiState.Editing(
                    photoPath = photoPath,
                    extractedText = result.extractedText,
                    category = result.category,
                    detectedEntities = result.detectedEntities,
                )
            } catch (t: Throwable) {
                _uiState.value = ReviewUiState.Error(t.message ?: "Something went wrong while processing the photo.")
            }
        }
    }

    fun updateText(text: String) {
        _uiState.update { state -> if (state is ReviewUiState.Editing) state.copy(extractedText = text) else state }
    }

    fun updateCategory(category: Category) {
        _uiState.update { state -> if (state is ReviewUiState.Editing) state.copy(category = category) else state }
    }

    fun save() {
        val current = _uiState.value
        if (current !is ReviewUiState.Editing || current.isSaving) return

        _uiState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            try {
                val bitmap = BitmapFactory.decodeFile(current.photoPath)
                    ?: error("Couldn't read the captured photo.")
                repository.saveNote(
                    bitmap = bitmap,
                    extractedText = current.extractedText,
                    category = current.category,
                    detectedEntities = current.detectedEntities,
                )
                _uiState.value = ReviewUiState.Saved
            } catch (t: Throwable) {
                _uiState.value = ReviewUiState.Error(t.message ?: "Something went wrong while saving the note.")
            }
        }
    }

    companion object {
        const val PHOTO_PATH_ARG = "photoPath"
    }
}
