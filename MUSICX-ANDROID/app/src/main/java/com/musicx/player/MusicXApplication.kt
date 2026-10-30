package com.musicx.player

import android.app.Application
import com.musicx.player.data.local.LocalLibraryStore
import com.musicx.player.data.remote.NetworkClientProvider
import com.musicx.player.data.repository.MusicRepository
import com.musicx.player.data.security.SecurePreferencesManager
import com.musicx.player.playback.MusicPlayerController

class MusicXApplication : Application() {

    lateinit var securePreferences: SecurePreferencesManager
    lateinit var networkClientProvider: NetworkClientProvider
    lateinit var localStore: LocalLibraryStore
    lateinit var repository: MusicRepository
    lateinit var playerController: MusicPlayerController

    override fun onCreate() {
        super.onCreate()
        instance = this

        securePreferences = SecurePreferencesManager(this)
        networkClientProvider = NetworkClientProvider(securePreferences)
        localStore = LocalLibraryStore(this)
        repository = MusicRepository(networkClientProvider, localStore)
        playerController = MusicPlayerController(this, repository, networkClientProvider)
    }

    companion object {
        lateinit var instance: MusicXApplication
            private set
    }
}
