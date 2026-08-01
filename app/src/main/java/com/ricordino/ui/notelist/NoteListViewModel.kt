package com.ricordino.ui.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricordino.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NotesRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    // The Home and Search Results screens from the product spec collapse into one
    // screen here — the search bar filters the same list in place rather than
    // navigating to a separate route.
    private val notes = query.flatMapLatest { q ->
        if (q.isBlank()) repository.getAllNotes() else repository.searchNotes(q)
    }

    val uiState = combine(query, notes) { q, n -> NoteListUiState(query = q, notes = n) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteListUiState())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }
}
