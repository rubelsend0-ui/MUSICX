package com.soniqxpro.repository

import android.util.Log
import com.soniqxpro.database.RecentDao
import com.soniqxpro.database.RecentEntity
import com.soniqxpro.database.SongEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentRepository @Inject constructor(private val dao: RecentDao) {
    fun observeSongs(limit: Int = 100): Flow<List<SongEntity>> = dao.observeSongs(limit)

    suspend fun markPlayed(songId: String) {
        try {
            dao.upsert(RecentEntity(songId, System.currentTimeMillis()))
        } catch (e: Exception) {
            Log.w("RecentRepository", "Could not record recently played", e)
        }
    }

    suspend fun clear(): Result<Unit> = runCatching { dao.clear() }
}
