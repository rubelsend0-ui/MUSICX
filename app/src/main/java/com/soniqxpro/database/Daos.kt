package com.soniqxpro.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Upsert
    suspend fun upsertAll(songs: List<SongEntity>)

    @Query("SELECT * FROM songs")
    fun observeAll(): Flow<List<SongEntity>>

    @Query("SELECT id FROM songs")
    suspend fun allIds(): List<String>

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}

@Dao
interface PlaylistDao {
    @Insert
    suspend fun insert(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: Long)

    @Query(
        "SELECT p.id AS id, p.name AS name, p.createdAt AS createdAt, " +
            "(SELECT COUNT(*) FROM playlist_tracks t WHERE t.playlistId = p.id) AS trackCount " +
            "FROM playlists p ORDER BY p.createdAt DESC"
    )
    fun observeAll(): Flow<List<PlaylistWithCount>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun observe(id: Long): Flow<PlaylistEntity?>

    @Query(
        "SELECT s.* FROM songs s INNER JOIN playlist_tracks t ON s.id = t.songId " +
            "WHERE t.playlistId = :playlistId ORDER BY t.position ASC"
    )
    fun observeSongs(playlistId: Long): Flow<List<SongEntity>>

    @Query("SELECT songId FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun songIds(playlistId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: PlaylistTrackEntity): Long

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeTrack(playlistId: Long, songId: String)

    @Query("UPDATE playlist_tracks SET position = :position WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun setPosition(playlistId: Long, songId: String, position: Int)

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun maxPosition(playlistId: Long): Int
}

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun remove(songId: String)

    @Query("SELECT COUNT(*) FROM favorites WHERE songId = :songId")
    suspend fun count(songId: String): Int

    @Query("SELECT songId FROM favorites")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT s.* FROM songs s INNER JOIN favorites f ON s.id = f.songId ORDER BY f.addedAt DESC")
    fun observeSongs(): Flow<List<SongEntity>>
}

@Dao
interface RecentDao {
    @Upsert
    suspend fun upsert(recent: RecentEntity)

    @Query(
        "SELECT s.* FROM songs s INNER JOIN recently_played r ON s.id = r.songId " +
            "ORDER BY r.playedAt DESC LIMIT :limit"
    )
    fun observeSongs(limit: Int): Flow<List<SongEntity>>

    @Query("DELETE FROM recently_played")
    suspend fun clear()
}
