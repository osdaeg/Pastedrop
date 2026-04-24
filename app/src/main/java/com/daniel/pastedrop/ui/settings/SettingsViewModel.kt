package com.daniel.pastedrop.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniel.pastedrop.data.local.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SettingsDataStore
) : ViewModel() {

    val serverUrl: StateFlow<String> = store.settings
        .map { it.serverUrl }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun saveServerUrl(url: String) {
        viewModelScope.launch { store.saveServerUrl(url) }
    }
}
