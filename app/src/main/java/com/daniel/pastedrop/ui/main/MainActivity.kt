package com.daniel.pastedrop.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.daniel.pastedrop.ui.settings.SettingsScreen
import com.daniel.pastedrop.ui.settings.SettingsViewModel
import com.daniel.pastedrop.ui.theme.PasteDropTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PasteDropTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "snippets") {

                    composable("snippets") {
                        val vm: MainViewModel = hiltViewModel()
                        val snippets   by vm.snippets.collectAsState()
                        val pending    by vm.pendingCount.collectAsState()
                        val refreshing by vm.refreshing.collectAsState()
                        val serverUrl  by vm.serverUrl.collectAsState()
                        SnippetsScreen(
                            snippets        = snippets,
                            pendingCount    = pending,
                            refreshing      = refreshing,
                            serverUrl       = serverUrl,
                            onRefresh       = vm::refresh,
                            onAddClick      = { navController.navigate("add") },
                            onSettingsClick = { navController.navigate("settings") },
                            onDelete        = vm::delete
                        )
                    }

                    composable("add") {
                        val vm: AddSnippetViewModel = hiltViewModel()
                        val state by vm.state.collectAsState()
                        AddSnippetScreen(
                            state            = state,
                            onTitleChange    = vm::setTitle,
                            onContentChange  = vm::setContent,
                            onLanguageChange = vm::setLanguage,
                            onTtlChange      = vm::setTtl,
                            onSave           = vm::save,
                            onBack           = { navController.popBackStack() }
                        )
                        if (state.saved) {
                            LaunchedEffect(Unit) { navController.popBackStack() }
                        }
                    }

                    composable("settings") {
                        val vm: SettingsViewModel = hiltViewModel()
                        val url by vm.serverUrl.collectAsState()
                        SettingsScreen(
                            currentUrl = url,
                            onSave  = { vm.saveServerUrl(it); navController.popBackStack() },
                            onBack  = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
