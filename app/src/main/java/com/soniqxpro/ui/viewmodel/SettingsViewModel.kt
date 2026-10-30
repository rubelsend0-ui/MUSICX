package com.soniqxpro.ui.viewmodel

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soniqxpro.model.AppSettings
import com.soniqxpro.model.SortOrder
import com.soniqxpro.model.ThemeMode
import com.soniqxpro.repository.LibraryRepository
import com.soniqxpro.repository.RecentRepository
import com.soniqxpro.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class StorageInfo(val totalBytes: Long = 0L, val freeBytes: Long = 0L, val cacheBytes: Long = 0L)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepo: SettingsRepository,
    private val library: LibraryRepository,
    private val recent: RecentRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _storage = MutableStateFlow(StorageInfo())
    val storage: StateFlow<StorageInfo> = _storage.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        refreshStorage()
    }

    fun refreshStorage() {
        viewModelScope.launch {
            _storage.value = withContext(Dispatchers.IO) { readStorage() }
        }
    }

    private fun readStorage(): StorageInfo = try {
        val stat = StatFs(Environment.getDataDirectory().path)
        val cache = context.cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        StorageInfo(stat.totalBytes, stat.availableBytes, cache)
    } catch (e: Exception) {
        StorageInfo()
    }

    fun setTheme(mode: ThemeMode) { viewModelScope.launch { settingsRepo.setThemeMode(mode) } }
    fun setAccent(index: Int) { viewModelScope.launch { settingsRepo.setAccent(index) } }
    fun setSort(order: SortOrder) { viewModelScope.launch { settingsRepo.setSortOrder(order) } }

    fun clearCache() {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
                    true
                } catch (e: Exception) {
                    false
                }
            }
            _messages.tryEmit(if (ok) "Cache cleared" else "Could not clear cache")
            refreshStorage()
        }
    }

    fun clearRecent() {
        viewModelScope.launch {
            recent.clear().fold(
                onSuccess = { _messages.tryEmit("Recently played cleared") },
                onFailure = { _messages.tryEmit(it.message ?: "Could not clear history") }
            )
        }
    }

    fun rescan() {
        viewModelScope.launch {
            library.scan().fold(
                onSuccess = { _messages.tryEmit("Library updated: $it tracks found") },
                onFailure = { _messages.tryEmit(it.message ?: "Library scan failed") }
            )
        }
    }
}
