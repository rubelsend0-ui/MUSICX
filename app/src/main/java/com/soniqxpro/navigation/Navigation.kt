package com.soniqxpro.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val LIBRARY = "library"
    const val PLAYLISTS = "playlists"
    const val FAVORITES = "favorites"
    const val RECENT = "recent"
    const val SETTINGS = "settings"
    const val PLAYLIST_DETAIL = "playlist/{id}"
    const val PLAYER = "player"

    fun playlistDetail(id: Long) = "playlist/$id"
}

data class BottomItem(val route: String, val label: String, val icon: ImageVector)

val bottomItems = listOf(
    BottomItem(Routes.LIBRARY, "Library", Icons.Rounded.LibraryMusic),
    BottomItem(Routes.PLAYLISTS, "Playlists", Icons.Rounded.QueueMusic),
    BottomItem(Routes.FAVORITES, "Favorites", Icons.Rounded.Favorite),
    BottomItem(Routes.RECENT, "Recent", Icons.Rounded.History),
    BottomItem(Routes.SETTINGS, "Settings", Icons.Rounded.Settings)
)
