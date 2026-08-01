package com.ricordino.ui.review

import com.ricordino.data.local.DetectedEntity
import com.ricordino.pipeline.Category

sealed interface ReviewUiState {
    data object Processing : ReviewUiState

    data class Editing(
        val photoPath: String,
        val extractedText: String,
        val category: Category,
        val detectedEntities: List<DetectedEntity>,
        val isSaving: Boolean = false,
    ) : ReviewUiState

    data class Error(val message: String) : ReviewUiState

    data object Saved : ReviewUiState
}
