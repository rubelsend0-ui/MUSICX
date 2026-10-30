package com.soniqxpro.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.soniqxpro.database.SongDao
import com.soniqxpro.database.SongEntity
import com.soniqxpro.util.hasAudioPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao
) {
    val songs: Flow<List<SongEntity>> = songDao.observeAll()

    /** Scans MediaStore and syncs the songs table. Returns the number of tracks found. */
    suspend fun scan(): Result<Int> = withContext(Dispatchers.IO) {
        if (!hasAudioPermission(context)) {
            return@withContext Result.failure<Int>(
                SecurityException("Audio permission not granted. Allow access from the Library tab.")
            )
        }
        runCatching {
            val found = queryMediaStore()
            songDao.upsertAll(found)
            if (found.isNotEmpty()) {
                val foundIds = found.map { it.id }.toHashSet()
                songDao.allIds().filter { it !in foundIds }.chunked(500).forEach { songDao.deleteByIds(it) }
            }
            found.size
        }
    }

    private fun queryMediaStore(): List<SongEntity> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val albumArtBase = Uri.parse("content://media/external/audio/albumart")
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 0"
        val out = ArrayList<SongEntity>()
        context.contentResolver.query(collection, projection, selection, null, null)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val addedCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val albumId = c.getLong(albumIdCol)
                val artist = c.getString(artistCol)?.takeUnless { it.isBlank() || it == "<unknown>" } ?: "Unknown artist"
                val album = c.getString(albumCol)?.takeUnless { it.isBlank() || it == "<unknown>" } ?: "Unknown album"
                val path = c.getString(dataCol)
                out += SongEntity(
                    id = "local:$id",
                    title = c.getString(titleCol)?.takeIf { it.isNotBlank() } ?: "Unknown title",
                    artist = artist,
                    album = album,
                    durationMs = c.getLong(durCol),
                    uri = ContentUris.withAppendedId(collection, id).toString(),
                    artworkUri = if (albumId > 0) ContentUris.withAppendedId(albumArtBase, albumId).toString() else null,
                    dateAdded = c.getLong(addedCol),
                    folder = path?.let { File(it).parent } ?: "Unknown folder"
                )
            }
        }
        return out
    }
}
