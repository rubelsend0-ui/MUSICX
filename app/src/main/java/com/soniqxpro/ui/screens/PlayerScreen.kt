package com.soniqxpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.soniqxpro.ui.components.Artwork
import com.soniqxpro.ui.components.EmptyState
import com.soniqxpro.ui.theme.LocalAccent
import com.soniqxpro.ui.theme.accentBrush
import com.soniqxpro.ui.viewmodel.PlayerViewModel
import com.soniqxpro.util.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(onBack: () -> Unit, vm: PlayerViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val isFavorite by vm.isFavorite.collectAsStateWithLifecycle()
    val accent = LocalAccent.current
    var showQueue by remember { mutableStateOf(false) }
    var speedMenu by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf<Float?>(null) }
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(accent.primary.copy(alpha = 0.28f), MaterialTheme.colorScheme.background))
        )
    ) {
        if (!s.hasMedia) {
            Column(Modifier.fillMaxSize()) {
                IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Close player")
                }
                EmptyState(Icons.Rounded.MusicNote, "Nothing is playing", "Pick a song from your library to start.")
            }
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Close player") }
                    Text("Now playing", Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = muted)
                    Spacer(Modifier.width(48.dp))
                }
                Spacer(Modifier.height(16.dp))
                Artwork(s.artworkUri, Modifier.fillMaxWidth().aspectRatio(1f), corner = 24.dp)
                Spacer(Modifier.height(24.dp))
                Text(s.title, style = MaterialTheme.typography.headlineMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(s.artist, style = MaterialTheme.typography.titleMedium, color = muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(16.dp))

                val duration = s.durationMs.coerceAtLeast(1L).toFloat()
                Slider(
                    value = dragging ?: s.positionMs.toFloat().coerceIn(0f, duration),
                    onValueChange = { dragging = it },
                    onValueChangeFinished = {
                        dragging?.let { vm.seekTo(it.toLong()) }
                        dragging = null
                    },
                    valueRange = 0f..duration,
                    enabled = s.durationMs > 0
                )
                Row(Modifier.fillMaxWidth()) {
                    Text(formatDuration((dragging ?: s.positionMs.toFloat()).toLong()), style = MaterialTheme.typography.bodySmall, color = muted)
                    Spacer(Modifier.weight(1f))
                    Text(formatDuration(s.durationMs), style = MaterialTheme.typography.bodySmall, color = muted)
                }
                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { vm.toggleShuffle() }) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle", tint = if (s.shuffle) MaterialTheme.colorScheme.primary else muted)
                    }
                    IconButton(onClick = { vm.previous() }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(36.dp))
                    }
                    Box(
                        Modifier.size(72.dp).clip(CircleShape).background(accentBrush()).clickable { vm.togglePlay() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (s.isBuffering && !s.isPlaying) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                        } else {
                            Icon(
                                if (s.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (s.isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    IconButton(onClick = { vm.next() }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp))
                    }
                    IconButton(onClick = { vm.cycleRepeat() }) {
                        Icon(
                            if (s.repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                            contentDescription = "Repeat",
                            tint = if (s.repeatMode == Player.REPEAT_MODE_OFF) muted else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { vm.toggleFavorite() }) {
                        Icon(
                            if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else muted
                        )
                    }
                    Box {
                        TextButton(onClick = { speedMenu = true }) { Text("${s.speed}x") }
                        DropdownMenu(expanded = speedMenu, onDismissRequest = { speedMenu = false }) {
                            listOf(0.75f, 1f, 1.25f, 1.5f, 2f).forEach { sp ->
                                DropdownMenuItem(
                                    text = { Text("${sp}x") },
                                    onClick = { speedMenu = false; vm.setSpeed(sp) }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { showQueue = true }) {
                        Icon(Icons.Rounded.QueueMusic, contentDescription = "Queue")
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showQueue) {
        ModalBottomSheet(onDismissRequest = { showQueue = false }) {
            Text(
                "Queue (${s.queue.size})",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            LazyColumn(Modifier.heightIn(max = 480.dp)) {
                itemsIndexed(s.queue) { index, item ->
                    val current = index == s.currentIndex
                    Row(
                        Modifier.fillMaxWidth().clickable { vm.skipTo(index) }.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Artwork(item.artworkUri, Modifier.size(44.dp), corner = 8.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.title,
                                color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(item.artist, style = MaterialTheme.typography.bodySmall, color = muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { vm.moveInQueue(index, index - 1) }, enabled = index > 0) {
                            Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Move up")
                        }
                        IconButton(onClick = { vm.moveInQueue(index, index + 1) }, enabled = index < s.queue.lastIndex) {
                            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Move down")
                        }
                        IconButton(onClick = { vm.removeFromQueue(index) }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Remove from queue")
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
