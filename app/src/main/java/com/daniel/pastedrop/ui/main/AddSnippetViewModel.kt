package com.daniel.pastedrop.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniel.pastedrop.domain.repository.PasteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddSnippetState(
    val title: String = "",
    val content: String = "",
    val language: String = "plaintext",
    val ttlSeconds: Int? = 604800,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddSnippetViewModel @Inject constructor(
    private val repository: PasteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddSnippetState())
    val state: StateFlow<AddSnippetState> = _state.asStateFlow()

    fun prefill(content: String, title: String? = null) {
        _state.update { it.copy(content = content, title = title ?: "") }
    }

    fun setTitle(v: String)    { _state.update { it.copy(title = v) } }
    fun setContent(v: String)  { _state.update { it.copy(content = v) } }
    fun setLanguage(v: String) { _state.update { it.copy(language = v) } }
    fun setTtl(v: Int?)        { _state.update { it.copy(ttlSeconds = v) } }

    fun save() {
        val s = _state.value
        if (s.content.isBlank()) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            repository.addSnippet(
                title      = s.title.ifBlank { null },
                content    = s.content,
                language   = s.language,
                ttlSeconds = s.ttlSeconds
            ).fold(
                onSuccess = { _state.update { it.copy(saving = false, saved = true) } },
                onFailure = { err -> _state.update { it.copy(saving = false, error = err.message) } }
            )
        }
    }
}
