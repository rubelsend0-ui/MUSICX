package com.soniqxpro.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.soniqxpro.navigation.Routes
import com.soniqxpro.navigation.bottomItems
import com.soniqxpro.ui.components.MiniPlayer
import com.soniqxpro.ui.screens.FavoritesScreen
import com.soniqxpro.ui.screens.LibraryScreen
import com.soniqxpro.ui.screens.PlayerScreen
import com.soniqxpro.ui.screens.PlaylistDetailScreen
import com.soniqxpro.ui.screens.PlaylistsScreen
import com.soniqxpro.ui.screens.RecentScreen
import com.soniqxpro.ui.screens.SettingsScreen
import com.soniqxpro.ui.viewmodel.PlayerViewModel

@Composable
fun SoniqAppRoot(playerVm: PlayerViewModel = hiltViewModel()) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val hasMedia by playerVm.hasMedia.collectAsStateWithLifecycle()
    val error by playerVm.error.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(error) {
        val message = error ?: return@LaunchedEffect
        val result = snackbar.showSnackbar(message, actionLabel = "Next")
        if (result == SnackbarResult.ActionPerformed) playerVm.next()
        playerVm.clearError()
    }

    val onPlayer = route == Routes.PLAYER

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (!onPlayer) {
                Column {
                    AnimatedVisibility(
                        visible = hasMedia,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        MiniPlayer(onClick = { nav.navigate(Routes.PLAYER) })
                    }
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)) {
                        bottomItems.forEach { item ->
                            val selected = route == item.route ||
                                (item.route == Routes.PLAYLISTS && route?.startsWith("playlist/") == true)
                            val scale by animateFloatAsState(if (selected) 1.15f else 1f, label = "navScale")
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    nav.navigate(item.route) {
                                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.scale(scale)) },
                                label = { Text(item.label, style = MaterialTheme.typography.labelMedium) }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = Routes.LIBRARY, modifier = Modifier.padding(padding)) {
            composable(Routes.LIBRARY) { LibraryScreen() }
            composable(Routes.PLAYLISTS) { PlaylistsScreen(onOpen = { nav.navigate(Routes.playlistDetail(it)) }) }
            composable(
                Routes.PLAYLIST_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { PlaylistDetailScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.FAVORITES) { FavoritesScreen() }
            composable(Routes.RECENT) { RecentScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
            composable(
                Routes.PLAYER,
                enterTransition = { slideInVertically(initialOffsetY = { it }) },
                exitTransition = { slideOutVertically(targetOffsetY = { it }) },
                popEnterTransition = { fadeIn() },
                popExitTransition = { slideOutVertically(targetOffsetY = { it }) }
            ) { PlayerScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
