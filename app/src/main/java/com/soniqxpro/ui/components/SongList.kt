package com.soniqxpro.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soniqxpro.database.SongEntity
import com.soniqxpro.ui.viewmodel.SongActionsViewModel
import com.soniqxpro.util.formatDuration

data class SongMenuAction(val label: String, val enabled: Boolean = true, val onClick: () -> Unit)

@Composable
fun SongList(
    songs: List<SongEntity>,
    modifier: Modifier = Modifier,
    extraActions: (SongEntity) -> List<SongMenuAction> = { emptyList() },
    vm: SongActionsViewModel = hiltViewModel()
) {
    val favoriteIds by vm.favoriteIds.collectAsStateWithLifecycle()
    val currentId by vm.currentMediaId.collectAsStateWithLifecycle()
    val playlists by vm.playlists.collectAsStateWithLifecycle()
    var pickFor by remember { mutableStateOf<SongEntity?>(null) }

    CollectMessages(vm.messages)

    LazyColumn(modifier, contentPadding = PaddingValues(bottom = 16.dp)) {
        itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
            SongRow(
                song = song,
                isCurrent = song.id == currentId,
                isFavorite = song.id in favoriteIds,
                onClick = { vm.play(songs, index) },
                onToggleFavorite = { vm.toggleFavorite(song) },
                onAddToPlaylist = { pickFor = song },
                extraActions = extraActions(song)
            )
        }
    }

    pickFor?.let { song ->
        AddToPlaylistDialog(
            playlists = playlists,
            onPick = { id -> vm.addToPlaylist(id, song); pickFor = null },
            onCreate = { name -> vm.createPlaylistAndAdd(name, song); pickFor = null },
            onDismiss = { pickFor = null }
        )
    }
}

@Composable
fun SongRow(
    song: SongEntity,
    isCurrent: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    extraActions: List<SongMenuAction>
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Artwork(song.artworkUri, Modifier.size(52.dp), corner = 10.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${song.artist} · ${formatDuration(song.durationMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Add to playlist") },
                    onClick = { menuOpen = false; onAddToPlaylist() }
                )
                extraActions.forEach { action ->
                    DropdownMenuItem(
                        text = { Text(action.label) },
                        enabled = action.enabled,
                        onClick = { menuOpen = false; action.onClick() }
                    )
                }
            }
        }
    }
}
