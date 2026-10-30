package com.soniqxpro.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.soniqxpro.database.SongEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class QueueItem(
    val mediaId: String,
    val title: String,
    val artist: String,
    val artworkUri: String?
)

data class PlayerState(
    val connected: Boolean = false,
    val hasMedia: Boolean = false,
    val mediaId: String? = null,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val artworkUri: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val speed: Float = 1f,
    val queue: List<QueueItem> = emptyList(),
    val currentIndex: Int = -1,
    val error: String? = null
)

/**
 * UI-side bridge to [PlaybackService] through a [MediaController].
 * All public functions must be called on the main thread (ViewModels already do).
 * Commands issued before the controller is connected are queued and run on connect.
 */
@Singleton
class PlayerConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var connecting = false
    private val pending = mutableListOf<(MediaController) -> Unit>()
    private var tickJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            refresh(rebuildQueue = events.contains(Player.EVENT_TIMELINE_CHANGED))
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.update { it.copy(error = playbackErrorMessage(error)) }
        }
    }

    private val controllerListener = object : MediaController.Listener {
        override fun onDisconnected(controller: MediaController) {
            this@PlayerConnection.controller = null
            tickJob?.cancel()
            _state.value = PlayerState(error = "Player service disconnected. It will reconnect on your next action.")
        }
    }

    fun connect() {
        if (controller != null || connecting) return
        connecting = true
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token)
            .setListener(controllerListener)
            .buildAsync()
        future.addListener(
            Runnable {
                connecting = false
                try {
                    val c = future.get()
                    controller = c
                    c.addListener(playerListener)
                    val queued = pending.toList()
                    pending.clear()
                    queued.forEach { it(c) }
                    refresh(rebuildQueue = true)
                } catch (e: Exception) {
                    pending.clear()
                    _state.update { it.copy(error = "Could not connect to the player service.") }
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun withController(block: (MediaController) -> Unit) {
        val c = controller
        if (c != null) {
            block(c)
        } else {
            pending += block
            connect()
        }
    }

    fun playQueue(songs: List<SongEntity>, startIndex: Int) = withController { c ->
        if (songs.isEmpty()) return@withController
        c.setMediaItems(songs.map { it.toMediaItem() }, startIndex.coerceIn(0, songs.lastIndex), 0L)
        c.prepare()
        c.play()
    }

    fun togglePlayPause() = withController { c ->
        if (c.isPlaying) {
            c.pause()
        } else {
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        }
    }

    fun next() = withController { it.seekToNextMediaItem() }
    fun previous() = withController { it.seekToPrevious() }
    fun seekTo(positionMs: Long) = withController { it.seekTo(positionMs) }
    fun setSpeed(speed: Float) = withController { it.setPlaybackSpeed(speed) }
    fun toggleShuffle() = withController { it.shuffleModeEnabled = !it.shuffleModeEnabled }

    fun cycleRepeat() = withController { c ->
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun skipTo(index: Int) = withController { c ->
        if (index in 0 until c.mediaItemCount) {
            c.seekToDefaultPosition(index)
            c.play()
        }
    }

    fun removeFromQueue(index: Int) = withController { c ->
        if (index in 0 until c.mediaItemCount) c.removeMediaItem(index)
    }

    fun moveInQueue(from: Int, to: Int) = withController { c ->
        if (from in 0 until c.mediaItemCount && to in 0 until c.mediaItemCount) c.moveMediaItem(from, to)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun refresh(rebuildQueue: Boolean) {
        val c = controller ?: return
        val item = c.currentMediaItem
        val md = item?.mediaMetadata
        val duration = c.duration
        _state.update { old ->
            old.copy(
                connected = true,
                hasMedia = item != null,
                mediaId = item?.mediaId,
                title = md?.title?.toString().orEmpty(),
                artist = md?.artist?.toString().orEmpty(),
                album = md?.albumTitle?.toString().orEmpty(),
                artworkUri = md?.artworkUri?.toString(),
                isPlaying = c.isPlaying,
                isBuffering = c.playbackState == Player.STATE_BUFFERING,
                positionMs = c.currentPosition.coerceAtLeast(0L),
                durationMs = if (duration == C.TIME_UNSET) 0L else duration.coerceAtLeast(0L),
                shuffle = c.shuffleModeEnabled,
                repeatMode = c.repeatMode,
                speed = c.playbackParameters.speed,
                queue = if (rebuildQueue) buildQueue(c) else old.queue,
                currentIndex = c.currentMediaItemIndex
            )
        }
        updateTicker(c.isPlaying)
    }

    private fun buildQueue(c: MediaController): List<QueueItem> =
        (0 until c.mediaItemCount).map { i ->
            val m = c.getMediaItemAt(i)
            QueueItem(
                mediaId = m.mediaId,
                title = m.mediaMetadata.title?.toString().orEmpty(),
                artist = m.mediaMetadata.artist?.toString().orEmpty(),
                artworkUri = m.mediaMetadata.artworkUri?.toString()
            )
        }

    private fun updateTicker(playing: Boolean) {
        tickJob?.cancel()
        if (!playing) return
        tickJob = scope.launch {
            while (isActive) {
                controller?.let { c ->
                    _state.update { it.copy(positionMs = c.currentPosition.coerceAtLeast(0L)) }
                }
                delay(500)
            }
        }
    }

    private fun playbackErrorMessage(e: PlaybackException): String = when (e.errorCode) {
        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
            "Audio file not found. It may have been moved or deleted."
        PlaybackException.ERROR_CODE_IO_NO_PERMISSION ->
            "Permission denied while reading the audio file."
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
            "Network problem while loading audio."
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
        PlaybackException.ERROR_CODE_DECODING_FAILED ->
            "This audio format is not supported or the file is corrupted."
        else -> "Playback failed (${e.errorCodeName})."
    }
}
