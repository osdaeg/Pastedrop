package com.daniel.pastedrop.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pastedrop_prefs")

data class AppSettings(
    val serverUrl: String = "http://192.168.1.10:8090"
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val SERVER_URL = stringPreferencesKey("server_url")

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(serverUrl = prefs[SERVER_URL] ?: "http://192.168.1.10:8090")
    }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { it[SERVER_URL] = url }
    }
}
