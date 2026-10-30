package com.musicx.player.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.musicx.player.data.model.Song
import com.musicx.player.data.remote.NetworkClientProvider
import com.musicx.player.data.repository.MusicRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicPlayerController(
    private val context: Context,
    private val repository: MusicRepository,
    private val network: NetworkClientProvider
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _currentDuration = MutableStateFlow(0L)
    val currentDuration: StateFlow<Long> = _currentDuration.asStateFlow()

    private var currentQueue: List<Song> = emptyList()
    private var currentIndex: Int = -1

    init {
        initializeController()
        startProgressTracker()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    _isPlaying.value = playing
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        playNext()
                    }
                }
            })
        }, MoreExecutors.directExecutor())
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty() || startIndex !in songs.indices) return
        currentQueue = songs
        currentIndex = startIndex
        playSong(songs[startIndex])
    }

    private fun playSong(song: Song) {
        _currentSong.value = song
        _currentDuration.value = song.durationMs
        scope.launch { repository.recordRecent(song) }

        val streamUrl = network.buildStreamUrl(song.id)
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.displayTitle)
                    .setArtist(song.displayArtist)
                    .setAlbumTitle(song.displayAlbum)
                    .build()
            )
            .build()

        controller?.run {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun togglePlayPause() {
        controller?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun playNext() {
        if (currentQueue.isEmpty()) return
        currentIndex = (currentIndex + 1) % currentQueue.size
        playSong(currentQueue[currentIndex])
    }

    fun playPrevious() {
        if (currentQueue.isEmpty()) return
        currentIndex = if (currentIndex - 1 < 0) currentQueue.size - 1 else currentIndex - 1
        playSong(currentQueue[currentIndex])
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    private fun startProgressTracker() {
        scope.launch {
            while (isActive) {
                controller?.let {
                    if (it.isPlaying) {
                        _currentPosition.value = it.currentPosition
                        if (it.duration > 0) _currentDuration.value = it.duration
                    }
                }
                delay(500)
            }
        }
    }
}
