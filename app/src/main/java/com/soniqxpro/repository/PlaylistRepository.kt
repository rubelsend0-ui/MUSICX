package com.soniqxpro.repository

import androidx.room.withTransaction
import com.soniqxpro.database.PlaylistDao
import com.soniqxpro.database.PlaylistEntity
import com.soniqxpro.database.PlaylistTrackEntity
import com.soniqxpro.database.PlaylistWithCount
import com.soniqxpro.database.SongEntity
import com.soniqxpro.database.SoniqDatabase
import com.soniqxpro.util.moved
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val db: SoniqDatabase,
    private val dao: PlaylistDao
) {
    val playlists: Flow<List<PlaylistWithCount>> = dao.observeAll()

    fun observePlaylist(id: Long): Flow<PlaylistEntity?> = dao.observe(id)
    fun observeSongs(id: Long): Flow<List<SongEntity>> = dao.observeSongs(id)

    suspend fun create(name: String): Result<Long> {
        val n = name.trim()
        if (n.isEmpty()) return Result.failure(IllegalArgumentException("Playlist name cannot be empty"))
        return runCatching { dao.insert(PlaylistEntity(name = n, createdAt = System.currentTimeMillis())) }
    }

    suspend fun rename(id: Long, name: String): Result<Unit> {
        val n = name.trim()
        if (n.isEmpty()) return Result.failure(IllegalArgumentException("Playlist name cannot be empty"))
        return runCatching { dao.rename(id, n) }
    }

    suspend fun delete(id: Long): Result<Unit> = runCatching { dao.delete(id) }

    /** Returns how many songs were actually added (duplicates are skipped). */
    suspend fun addSongs(playlistId: Long, songIds: List<String>): Result<Int> = runCatching {
        db.withTransaction {
            var position = dao.maxPosition(playlistId)
            var added = 0
            songIds.forEach { songId ->
                if (dao.insertTrack(PlaylistTrackEntity(playlistId, songId, position + 1)) != -1L) {
                    position++
                    added++
                }
            }
            added
        }
    }

    suspend fun removeSong(playlistId: Long, songId: String): Result<Unit> =
        runCatching { dao.removeTrack(playlistId, songId) }

    suspend fun move(playlistId: Long, from: Int, to: Int): Result<Unit> = runCatching {
        db.withTransaction {
            dao.songIds(playlistId).moved(from, to).forEachIndexed { index, id ->
                dao.setPosition(playlistId, id, index)
            }
        }
    }
}
