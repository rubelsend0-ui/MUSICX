package com.soniqxpro.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.soniqxpro.model.AppSettings
import com.soniqxpro.model.SortOrder
import com.soniqxpro.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val accent = intPreferencesKey("accent")
        val sort = stringPreferencesKey("sort")
    }

    val settings: Flow<AppSettings> = context.settingsStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            AppSettings(
                themeMode = p[Keys.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.DARK,
                accentIndex = p[Keys.accent] ?: 0,
                sortOrder = p[Keys.sort]?.let { runCatching { SortOrder.valueOf(it) }.getOrNull() } ?: SortOrder.TITLE
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsStore.edit { it[Keys.theme] = mode.name }
    }

    suspend fun setAccent(index: Int) {
        context.settingsStore.edit { it[Keys.accent] = index }
    }

    suspend fun setSortOrder(order: SortOrder) {
        context.settingsStore.edit { it[Keys.sort] = order.name }
    }
}
