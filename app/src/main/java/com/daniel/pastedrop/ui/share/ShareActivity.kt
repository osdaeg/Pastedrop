package com.daniel.pastedrop.ui.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.daniel.pastedrop.ui.main.AddSnippetScreen
import com.daniel.pastedrop.ui.main.AddSnippetViewModel
import com.daniel.pastedrop.ui.theme.PasteDropTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class ShareActivity : ComponentActivity() {

    private val viewModel: AddSnippetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            else -> ""
        }
        val sharedTitle = intent?.getStringExtra(Intent.EXTRA_SUBJECT)

        if (sharedText.isNotBlank()) {
            viewModel.prefill(sharedText, sharedTitle)
        }

        setContent {
            PasteDropTheme {
                val state by viewModel.state.collectAsState()

                AddSnippetScreen(
                    state           = state,
                    onTitleChange   = viewModel::setTitle,
                    onContentChange = viewModel::setContent,
                    onLanguageChange = viewModel::setLanguage,
                    onTtlChange     = viewModel::setTtl,
                    onSave          = viewModel::save,
                    onBack          = { finish() }
                )

                if (state.saved) {
                    LaunchedEffect(Unit) {
                        delay(800)
                        finish()
                    }
                }
            }
        }
    }
}
