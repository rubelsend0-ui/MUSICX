package com.musicx.player.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicx.player.MusicXApplication
import com.musicx.player.data.model.Song
import com.musicx.player.ui.components.GlassCard
import com.musicx.player.ui.components.SongListItem
import com.musicx.player.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onOpenSong: (Song) -> Unit) {
    val repo = MusicXApplication.instance.repository
    val controller = MusicXApplication.instance.playerController

    var librarySongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var totalSongs by remember { mutableIntStateOf(0) }
    var statusText by remember { mutableStateOf("Checking API...") }

    LaunchedEffect(Unit) {
        try {
            val health = repo.getHealth()
            statusText = if (health.status == "healthy") "Online • Self-Hosted" else "API degraded"
            val lib = repo.getLibrary(1, 20)
            librarySongs = lib.songs
            totalSongs = lib.pagination?.total ?: lib.songs.size
        } catch (e: Exception) {
            statusText = "Offline / Connection error"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MUSICX", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = ElectricBlue)
                    Text("Personal Audio Cloud", fontSize = 13.sp, color = TextSecondary)
                }
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = if (statusText.startsWith("Online")) ElectricBlue else PurpleAccent
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Library Overview", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("$totalSongs indexed songs", fontSize = 12.sp, color = TextSecondary)
                    }
                    Button(
                        onClick = { if (librarySongs.isNotEmpty()) controller.playQueue(librarySongs.shuffled()) },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, tint = DeepBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shuffle All", color = DeepBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Recently Added", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(librarySongs) { song ->
            SongListItem(song = song) {
                controller.playQueue(librarySongs, librarySongs.indexOf(song))
                onOpenSong(song)
            }
        }
    }
}

@Composable
fun SearchScreen(onOpenSong: (Song) -> Unit) {
    val repo = MusicXApplication.instance.repository
    val controller = MusicXApplication.instance.playerController
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Song>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                scope.launch {
                    if (it.isNotBlank()) {
                        try {
                            results = repo.search(it.trim()).results
                        } catch (e: Exception) {}
                    }
                }
            },
            placeholder = { Text("Search title, artist, album...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricBlue) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = DarkSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(results) { song ->
                SongListItem(song = song) {
                    controller.playQueue(results, results.indexOf(song))
                    onOpenSong(song)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val app = MusicXApplication.instance
    var url by remember { mutableStateOf(app.securePreferences.getBaseUrl()) }
    var token by remember { mutableStateOf(app.securePreferences.getApiToken()) }
    var resultText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(20.dp)
    ) {
        Text("API Configuration", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Connect to your Termux or Cloudflare endpoint securely.", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Server URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("API Bearer Token") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row {
            Button(
                onClick = {
                    app.securePreferences.saveApiConfig(url, token)
                    scope.launch {
                        try {
                            val h = app.repository.getHealth()
                            resultText = "Connected! Status: ${h.status}"
                        } catch (e: Exception) {
                            resultText = "Connection Failed: ${e.message}"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("Save & Test Connection", color = DeepBlack, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        if (resultText.isNotBlank()) {
            Text(resultText, color = if (resultText.startsWith("Connected")) ElectricBlue else PurpleAccent)
        }
    }
}

@Composable
fun NowPlayingScreen(onClose: () -> Unit) {
    val controller = MusicXApplication.instance.playerController
    val song by controller.currentSong.collectAsState()
    val isPlaying by controller.isPlaying.collectAsState()
    val position by controller.currentPosition.collectAsState()
    val duration by controller.currentDuration.collectAsState()

    if (song == null) {
        onClose()
        return
    }

    val current = song!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterVertically,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextPrimary)
            }
            Text("NOW PLAYING", fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 12.sp)
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = TextPrimary)
            }
        }

        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.radialGradient(listOf(PurpleAccent, ElectricBlue, DeepBlack))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = DeepBlack, modifier = Modifier.size(100.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(current.displayTitle, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(current.displayArtist, fontSize = 15.sp, color = TextSecondary)
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = if (duration > 0) position.toFloat() / duration.toFloat() else 0f,
                onValueChange = { frac ->
                    controller.seekTo((frac * duration).toLong())
                },
                colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(position), color = TextSecondary, fontSize = 12.sp)
                Text(formatTime(duration), color = TextSecondary, fontSize = 12.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { controller.playPrevious() }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(36.dp))
            }
            FloatingActionButton(
                onClick = { controller.togglePlayPause() },
                containerColor = ElectricBlue,
                contentColor = DeepBlack,
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = { controller.playNext() }) {
                Icon(Icons.Default.SkipNext, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(36.dp))
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
