package com.daniel.pastedrop.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

val LANGUAGES = listOf(
    "plaintext", "python", "bash", "javascript", "typescript",
    "json", "yaml", "toml", "html", "css", "sql", "go", "rust",
    "c", "cpp", "java", "kotlin", "ruby", "php", "markdown", "xml"
)

val TTL_OPTIONS = listOf(
    "Nunca" to null,
    "1 hora" to 3600,
    "6 horas" to 21600,
    "1 día" to 86400,
    "7 días" to 604800,
    "30 días" to 2592000
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSnippetScreen(
    state: AddSnippetState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onTtlChange: (Int?) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    var langExpanded by remember { mutableStateOf(false) }
    var ttlExpanded  by remember { mutableStateOf(false) }

    val ttlLabel = TTL_OPTIONS.find { it.second == state.ttlSeconds }?.first ?: "7 días"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo snippet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Título
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Título (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Lenguaje
            ExposedDropdownMenuBox(
                expanded = langExpanded,
                onExpandedChange = { langExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.language,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lenguaje") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                )
                ExposedDropdownMenu(expanded = langExpanded, onDismissRequest = { langExpanded = false }) {
                    LANGUAGES.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang, fontFamily = FontFamily.Monospace) },
                            onClick = { onLanguageChange(lang); langExpanded = false }
                        )
                    }
                }
            }

            // TTL
            ExposedDropdownMenuBox(
                expanded = ttlExpanded,
                onExpandedChange = { ttlExpanded = it }
            ) {
                OutlinedTextField(
                    value = ttlLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Expira en") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ttlExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(expanded = ttlExpanded, onDismissRequest = { ttlExpanded = false }) {
                    TTL_OPTIONS.forEach { (label, seconds) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = { onTtlChange(seconds); ttlExpanded = false }
                        )
                    }
                }
            }

            // Contenido
            OutlinedTextField(
                value = state.content,
                onValueChange = onContentChange,
                label = { Text("Contenido") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                shape = RoundedCornerShape(10.dp),
                isError = state.content.isBlank() && state.error != null
            )

            // Error
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Botón guardar
            Button(
                onClick = onSave,
                enabled = state.content.isNotBlank() && !state.saving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (state.saving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.saving) "Guardando..." else "Guardar snippet")
            }
        }
    }
}
