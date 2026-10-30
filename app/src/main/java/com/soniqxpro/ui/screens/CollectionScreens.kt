package com.soniqxpro.ui.screens

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
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soniqxpro.database.PlaylistWithCount
import com.soniqxpro.ui.components.AddSongsDialog
import com.soniqxpro.ui.components.CollectMessages
import com.soniqxpro.ui.components.EmptyState
import com.soniqxpro.ui.components.LoadingState
import com.soniqxpro.ui.components.NameDialog
import com.soniqxpro.ui.components.SongList
import com.soniqxpro.ui.components.SongMenuAction
import com.soniqxpro.ui.theme.accentBrush
import com.soniqxpro.ui.viewmodel.FavoritesViewModel
import com.soniqxpro.ui.viewmodel.PlaylistDetailViewModel
import com.soniqxpro.ui.viewmodel.PlaylistsViewModel
import com.soniqxpro.ui.viewmodel.RecentViewModel
import com.soniqxpro.util.songCountLabel

@Composable
private fun ScreenHeader(title: String, trailing: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
fun FavoritesScreen(vm: FavoritesViewModel = hiltViewModel()) {
    val songs by vm.songs.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Favorites")
        val list = songs
        when {
            list == null -> LoadingState()
            list.isEmpty() -> EmptyState(
                Icons.Rounded.Favorite, "No favorites yet",
                "Tap the heart on any song to keep it here."
            )
            else -> SongList(list, Modifier.weight(1f))
        }
    }
}

@Composable
fun RecentScreen(vm: RecentViewModel = hiltViewModel()) {
    val songs by vm.songs.collectAsStateWithLifecycle()
    CollectMessages(vm.messages)
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Recently played") {
            if (!songs.isNullOrEmpty()) TextButton(onClick = { vm.clear() }) { Text("Clear") }
        }
        val list = songs
        when {
            list == null -> LoadingState()
            list.isEmpty() -> EmptyState(
                Icons.Rounded.History, "Nothing played yet",
                "Songs you play will show up here."
            )
            else -> SongList(list, Modifier.weight(1f))
        }
    }
}

@Composable
fun PlaylistsScreen(onOpen: (Long) -> Unit, vm: PlaylistsViewModel = hiltViewModel()) {
    val playlists by vm.playlists.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<PlaylistWithCount?>(null) }
    var deleteTarget by remember { mutableStateOf<PlaylistWithCount?>(null) }
    CollectMessages(vm.messages)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Playlists") {
            IconButton(onClick = { showCreate = true }) { Icon(Icons.Rounded.Add, contentDescription = "New playlist") }
        }
        val list = playlists
        when {
            list == null -> LoadingState()
            list.isEmpty() -> EmptyState(
                Icons.Rounded.QueueMusic, "No playlists yet",
                "Create a playlist and add songs from your library.",
                actionLabel = "New playlist", onAction = { showCreate = true }
            )
            else -> LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(list, key = { it.id }) { p ->
                    var menu by remember { mutableStateOf(false) }
                    Row(
                        Modifier.fillMaxWidth().clickable { onOpen(p.id) }.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(accentBrush(0.35f)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Rounded.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                songCountLabel(p.trackCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box {
                            IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = "More options") }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                DropdownMenuItem(text = { Text("Rename") }, onClick = { menu = false; renameTarget = p })
                                DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; deleteTarget = p })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        NameDialog("New playlist", "", "Create", onConfirm = { vm.create(it); showCreate = false }, onDismiss = { showCreate = false })
    }
    renameTarget?.let { p ->
        NameDialog("Rename playlist", p.name, "Save", onConfirm = { vm.rename(p.id, it); renameTarget = null }, onDismiss = { renameTarget = null })
    }
    deleteTarget?.let { p ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete playlist?") },
            text = { Text("\"${p.name}\" will be deleted. Your songs are not affected.") },
            confirmButton = { TextButton(onClick = { vm.delete(p.id); deleteTarget = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun PlaylistDetailScreen(onBack: () -> Unit, vm: PlaylistDetailViewModel = hiltViewModel()) {
    val playlist by vm.playlist.collectAsStateWithLifecycle()
    val songs by vm.songs.collectAsStateWithLifecycle()
    val library by vm.librarySongs.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    CollectMessages(vm.messages)

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
            Column(Modifier.weight(1f)) {
                Text(playlist?.name.orEmpty(), style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(songCountLabel(songs.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.playAll(false) }, enabled = songs.isNotEmpty()) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("Play")
            }
            OutlinedButton(onClick = { vm.playAll(true) }, enabled = songs.isNotEmpty()) {
                Icon(Icons.Rounded.Shuffle, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("Shuffle")
            }
            OutlinedButton(onClick = { showAdd = true }) {
                Icon(Icons.Rounded.Add, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("Add")
            }
        }
        Spacer(Modifier.size(8.dp))
        if (songs.isEmpty()) {
            EmptyState(
                Icons.Rounded.QueueMusic, "This playlist is empty",
                "Add songs here, or use \"Add to playlist\" on any song.",
                actionLabel = "Add songs", onAction = { showAdd = true }
            )
        } else {
            SongList(
                songs,
                Modifier.weight(1f),
                extraActions = { song ->
                    val i = songs.indexOfFirst { it.id == song.id }
                    listOf(
                        SongMenuAction("Move up", enabled = i > 0) { vm.move(i, i - 1) },
                        SongMenuAction("Move down", enabled = i in 0 until songs.lastIndex) { vm.move(i, i + 1) },
                        SongMenuAction("Remove from playlist") { vm.removeSong(song) }
                    )
                }
            )
        }
    }

    if (showAdd) {
        val inPlaylist = songs.map { it.id }.toSet()
        AddSongsDialog(
            candidates = library.filter { it.id !in inPlaylist },
            onConfirm = { vm.addSongs(it); showAdd = false },
            onDismiss = { showAdd = false }
        )
    }
}
