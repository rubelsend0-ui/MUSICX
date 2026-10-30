package com.soniqxpro.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import com.soniqxpro.model.SortOrder
import com.soniqxpro.ui.components.Artwork
import com.soniqxpro.ui.components.EmptyState
import com.soniqxpro.ui.components.ErrorState
import com.soniqxpro.ui.components.LoadingState
import com.soniqxpro.ui.components.SongList
import com.soniqxpro.ui.viewmodel.LibraryViewModel
import com.soniqxpro.util.GroupType
import com.soniqxpro.util.SongGroup
import com.soniqxpro.util.audioPermission
import com.soniqxpro.util.groupInto
import com.soniqxpro.util.hasAudioPermission
import com.soniqxpro.util.matchesQuery
import com.soniqxpro.util.openAppSettings
import com.soniqxpro.util.sortedByOrder

@Composable
fun LibraryScreen(vm: LibraryViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val songs by vm.songs.collectAsStateWithLifecycle()
    val scan by vm.scanState.collectAsStateWithLifecycle()
    val sort by vm.sortOrder.collectAsStateWithLifecycle()

    var granted by remember { mutableStateOf(hasAudioPermission(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LifecycleResumeEffect(Unit) {
        granted = hasAudioPermission(context)
        onPauseOrDispose { }
    }
    LaunchedEffect(granted) { if (granted) vm.scan() }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var openGroup by rememberSaveable { mutableStateOf<String?>(null) }
    var sortMenu by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Library", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            if (granted) {
                IconButton(onClick = { vm.scan() }) { Icon(Icons.Rounded.Refresh, contentDescription = "Rescan library") }
                Box {
                    IconButton(onClick = { sortMenu = true }) { Icon(Icons.Rounded.Sort, contentDescription = "Sort") }
                    DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                        SortOrder.values().forEach { order ->
                            DropdownMenuItem(
                                text = { Text((if (order == sort) "• " else "") + order.label) },
                                onClick = { sortMenu = false; vm.setSort(order) }
                            )
                        }
                    }
                }
            }
        }

        if (!granted) {
            EmptyState(
                icon = Icons.Rounded.Lock,
                title = "Allow access to your music",
                message = "SONIQ X PRO needs permission to read audio files on this device. Nothing leaves your phone.",
                actionLabel = "Allow access",
                onAction = { launcher.launch(audioPermission()) },
                secondaryLabel = "Open app settings",
                onSecondary = { openAppSettings(context) }
            )
        } else {
            val all = songs.orEmpty()
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; openGroup = null },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search your library") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, contentDescription = "Clear search") }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                listOf("Songs", "Albums", "Artists", "Folders").forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i; openGroup = null }, text = { Text(label) })
                }
            }

            val filtered = remember(all, query, sort) {
                all.filter { query.isBlank() || it.matchesQuery(query.trim()) }.sortedByOrder(sort)
            }
            when {
                songs == null -> LoadingState()
                scan.error != null && all.isEmpty() -> ErrorState(scan.error ?: "Scan failed", onRetry = { vm.scan() })
                all.isEmpty() && scan.scanning -> LoadingState()
                all.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.MusicNote,
                    title = "No music found",
                    message = "No audio files were found on this device. Add some music, then rescan.",
                    actionLabel = "Rescan",
                    onAction = { vm.scan() }
                )
                filtered.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.Search,
                    title = "No matches",
                    message = "Nothing in your library matches \"$query\"."
                )
                tab == 0 -> SongList(filtered, Modifier.weight(1f))
                else -> {
                    val type = when (tab) {
                        1 -> GroupType.ALBUM
                        2 -> GroupType.ARTIST
                        else -> GroupType.FOLDER
                    }
                    GroupedContent(
                        groups = remember(filtered, type) { filtered.groupInto(type) },
                        openId = openGroup,
                        onOpen = { openGroup = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupedContent(
    groups: List<SongGroup>,
    openId: String?,
    onOpen: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val open = groups.firstOrNull { it.id == openId }
    Column(modifier) {
        if (open != null) {
            BackHandler { onOpen(null) }
            Row(
                Modifier.fillMaxWidth().clickable { onOpen(null) }.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onOpen(null) }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", modifier = Modifier.size(28.dp))
                }
                Text(open.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            SongList(open.songs, Modifier.weight(1f))
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(groups, key = { it.id }) { g ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onOpen(g.id) }.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Artwork(g.artworkUri, Modifier.size(52.dp), corner = 10.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(g.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                g.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
