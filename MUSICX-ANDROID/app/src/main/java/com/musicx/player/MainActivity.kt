package com.musicx.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.musicx.player.ui.components.MiniPlayer
import com.musicx.player.ui.screens.*
import com.musicx.player.ui.theme.DeepBlack
import com.musicx.player.ui.theme.MusicXTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicXTheme {
                val navController = rememberNavController()
                var isNowPlayingOpen by remember { mutableStateOf(false) }

                if (isNowPlayingOpen) {
                    NowPlayingScreen(onClose = { isNowPlayingOpen = false })
                } else {
                    Scaffold(
                        bottomBar = {
                            NavigationBar(containerColor = DeepBlack) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    selected = true,
                                    onClick = { navController.navigate("home") }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                    label = { Text("Search") },
                                    selected = false,
                                    onClick = { navController.navigate("search") }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") },
                                    selected = false,
                                    onClick = { navController.navigate("settings") }
                                )
                            }
                        }
                    ) { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            NavHost(navController = navController, startDestination = "home") {
                                composable("home") { HomeScreen(onOpenSong = { isNowPlayingOpen = true }) }
                                composable("search") { SearchScreen(onOpenSong = { isNowPlayingOpen = true }) }
                                composable("settings") { SettingsScreen() }
                            }
                            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                                MiniPlayer(
                                    controller = MusicXApplication.instance.playerController,
                                    onOpenNowPlaying = { isNowPlayingOpen = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
