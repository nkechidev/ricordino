package com.ricordino.ui.notelist

import com.ricordino.data.local.NoteEntity

data class NoteListUiState(
    val query: String = "",
    val notes: List<NoteEntity> = emptyList(),
)
