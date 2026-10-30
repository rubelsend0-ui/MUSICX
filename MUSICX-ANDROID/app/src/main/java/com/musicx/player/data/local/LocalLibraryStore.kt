package com.musicx.player.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.musicx.player.data.model.Playlist
import com.musicx.player.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "musicx_prefs")

class LocalLibraryStore(private val context: Context) {
    private val gson = Gson()
    private val FAV_KEY = stringPreferencesKey("favorites")
    private val RECENTS_KEY = stringPreferencesKey("recents")
    private val PLAYLISTS_KEY = stringPreferencesKey("playlists")

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map {
        val json = it[FAV_KEY] ?: "[]"
        gson.fromJson(json, object : TypeToken<Set<String>>() {}.type) ?: emptySet()
    }

    val recentsFlow: Flow<List<Song>> = context.dataStore.data.map {
        val json = it[RECENTS_KEY] ?: "[]"
        gson.fromJson(json, object : TypeToken<List<Song>>() {}.type) ?: emptyList()
    }

    val playlistsFlow: Flow<List<Playlist>> = context.dataStore.data.map {
        val json = it[PLAYLISTS_KEY] ?: "[]"
        gson.fromJson(json, object : TypeToken<List<Playlist>>() {}.type) ?: emptyList()
    }

    suspend fun toggleFavorite(songId: String) {
        context.dataStore.edit {
            val json = it[FAV_KEY] ?: "[]"
            val current: MutableSet<String> = gson.fromJson(json, object : TypeToken<MutableSet<String>>() {}.type) ?: mutableSetOf()
            if (current.contains(songId)) current.remove(songId) else current.add(songId)
            it[FAV_KEY] = gson.toJson(current)
        }
    }

    suspend fun addRecent(song: Song) {
        context.dataStore.edit {
            val json = it[RECENTS_KEY] ?: "[]"
            val current: MutableList<Song> = gson.fromJson(json, object : TypeToken<MutableList<Song>>() {}.type) ?: mutableListOf()
            current.removeAll { it.id == song.id }
            current.add(0, song)
            it[RECENTS_KEY] = gson.toJson(current.take(30))
        }
    }

    suspend fun createPlaylist(name: String) {
        context.dataStore.edit {
            val json = it[PLAYLISTS_KEY] ?: "[]"
            val current: MutableList<Playlist> = gson.fromJson(json, object : TypeToken<MutableList<Playlist>>() {}.type) ?: mutableListOf()
            current.add(0, Playlist(java.util.UUID.randomUUID().toString(), name))
            it[PLAYLISTS_KEY] = gson.toJson(current)
        }
    }

    suspend fun addSongToPlaylist(playlistId: String, songId: String) {
        context.dataStore.edit {
            val json = it[PLAYLISTS_KEY] ?: "[]"
            val current: MutableList<Playlist> = gson.fromJson(json, object : TypeToken<MutableList<Playlist>>() {}.type) ?: mutableListOf()
            val idx = current.indexOfFirst { it.id == playlistId }
            if (idx != -1) {
                val pl = current[idx]
                if (!pl.songIds.contains(songId)) {
                    current[idx] = pl.copy(songIds = pl.songIds + songId)
                    it[PLAYLISTS_KEY] = gson.toJson(current)
                }
            }
        }
    }
}
