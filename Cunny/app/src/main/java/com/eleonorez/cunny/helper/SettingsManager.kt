package com.eleonorez.cunny.helper

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val NARRATION_KEY = booleanPreferencesKey("narration_on")
        val SOUND_KEY = booleanPreferencesKey("sound_on")
        val THEME_KEY = stringPreferencesKey("theme")
    }

    val narrationFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NARRATION_KEY] ?: true
    }

    val soundFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SOUND_KEY] ?: true
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "light"
    }

    suspend fun saveNarration(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NARRATION_KEY] = enabled
        }
    }

    suspend fun saveSound(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_KEY] = enabled
        }
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }
}
