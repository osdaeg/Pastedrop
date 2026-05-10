package com.daniel.pastedrop.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniel.pastedrop.data.local.SettingsDataStore
import com.daniel.pastedrop.domain.model.Snippet
import com.daniel.pastedrop.domain.repository.PasteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PasteRepository,
    private val settings: SettingsDataStore
) : ViewModel() {

    val snippets: StateFlow<List<Snippet>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> = repository.observePendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val serverUrl: StateFlow<String> = settings.settings
        .map { it.serverUrl }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            repository.syncPending()
            repository.refreshFromServer()
            _refreshing.value = false
        }
    }

    fun delete(snippet: Snippet) {
        viewModelScope.launch { repository.deleteSnippet(snippet) }
    }
}
