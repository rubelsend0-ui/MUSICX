package com.soniqxpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soniqxpro.BuildConfig
import com.soniqxpro.model.SortOrder
import com.soniqxpro.model.ThemeMode
import com.soniqxpro.ui.components.CollectMessages
import com.soniqxpro.ui.theme.AccentPalettes
import com.soniqxpro.ui.viewmodel.SettingsViewModel
import com.soniqxpro.util.formatBytes

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val storage by vm.storage.collectAsStateWithLifecycle()
    CollectMessages(vm.messages)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 12.dp))

        Section("Theme") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.values().forEach { mode ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { vm.setTheme(mode) },
                        label = { Text(mode.label) }
                    )
                }
            }
        }

        Section("Accent color") {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AccentPalettes.forEachIndexed { index, palette ->
                    val selected = settings.accentIndex == index
                    Box(
                        Modifier.size(40.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(palette.primary, palette.secondary)))
                            .border(if (selected) 3.dp else 0.dp, if (selected) Color.White else Color.Transparent, CircleShape)
                            .clickable { vm.setAccent(index) }
                    )
                }
            }
        }

        Section("Library") {
            Text("Default sort order", style = MaterialTheme.typography.bodySmall, color = muted)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortOrder.values().forEach { order ->
                    FilterChip(
                        selected = settings.sortOrder == order,
                        onClick = { vm.setSort(order) },
                        label = { Text(order.label) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { vm.rescan() }) { Text("Rescan library") }
        }

        Section("Storage") {
            Text("Device free: ${formatBytes(storage.freeBytes)} of ${formatBytes(storage.totalBytes)}", color = muted)
            Text("App cache: ${formatBytes(storage.cacheBytes)}", color = muted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { vm.clearCache() }) { Text("Clear cache") }
                OutlinedButton(onClick = { vm.clearRecent() }) { Text("Clear recently played") }
            }
        }

        Section("About") {
            Text("SONIQ X PRO", style = MaterialTheme.typography.titleMedium)
            Text("Version ${BuildConfig.VERSION_NAME} (Phase 1)", color = muted)
        }

        Section("Privacy") {
            Text(
                "This version works fully on your device. It does not use the internet, has no accounts, " +
                    "and sends no analytics. Your playlists, favorites and history are stored locally and " +
                    "are removed when you uninstall the app.",
                color = muted
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}
