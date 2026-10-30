package com.soniqxpro.di

import android.content.Context
import androidx.room.Room
import com.soniqxpro.database.FavoriteDao
import com.soniqxpro.database.PlaylistDao
import com.soniqxpro.database.RecentDao
import com.soniqxpro.database.SongDao
import com.soniqxpro.database.SoniqDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SoniqDatabase =
        Room.databaseBuilder(context, SoniqDatabase::class.java, "soniq.db").build()

    @Provides
    fun provideSongDao(db: SoniqDatabase): SongDao = db.songDao()

    @Provides
    fun providePlaylistDao(db: SoniqDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideFavoriteDao(db: SoniqDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideRecentDao(db: SoniqDatabase): RecentDao = db.recentDao()
}
