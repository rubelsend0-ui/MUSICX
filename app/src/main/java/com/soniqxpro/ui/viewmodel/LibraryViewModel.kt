package com.soniqxpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soniqxpro.database.SongEntity
import com.soniqxpro.model.SortOrder
import com.soniqxpro.repository.LibraryRepository
import com.soniqxpro.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanState(val scanning: Boolean = false, val error: String? = null)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val library: LibraryRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    /** null = still loading from the database. */
    val songs: StateFlow<List<SongEntity>?> = library.songs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sortOrder: StateFlow<SortOrder> = settings.settings.map { it.sortOrder }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortOrder.TITLE)

    private val _scan = MutableStateFlow(ScanState())
    val scanState: StateFlow<ScanState> = _scan.asStateFlow()

    fun scan() {
        if (_scan.value.scanning) return
        viewModelScope.launch {
            _scan.update { ScanState(scanning = true) }
            val result = library.scan()
            _scan.value = ScanState(scanning = false, error = result.exceptionOrNull()?.let { it.message ?: "Library scan failed" })
        }
    }

    fun setSort(order: SortOrder) {
        viewModelScope.launch { settings.setSortOrder(order) }
    }
}
