package com.soniqxpro.repository

import com.soniqxpro.database.FavoriteDao
import com.soniqxpro.database.FavoriteEntity
import com.soniqxpro.database.SongEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(private val dao: FavoriteDao) {
    fun observeIds(): Flow<List<String>> = dao.observeIds()
    fun observeSongs(): Flow<List<SongEntity>> = dao.observeSongs()

    /** Returns the new favorite state (true = now a favorite). */
    suspend fun toggle(songId: String): Result<Boolean> = runCatching {
        if (dao.count(songId) > 0) {
            dao.remove(songId)
            false
        } else {
            dao.add(FavoriteEntity(songId, System.currentTimeMillis()))
            true
        }
    }
}
