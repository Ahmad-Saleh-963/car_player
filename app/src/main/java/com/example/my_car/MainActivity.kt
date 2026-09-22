package com.example.my_car

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.my_car.data.model.MediaTrack
import com.example.my_car.data.repository.MediaIndexCache
import com.example.my_car.data.repository.MediaStoreRepository
import com.example.my_car.service.MyMusicService
import com.example.my_car.ui.*
import com.example.my_car.ui.components.AutomotiveGestureFrame
import com.example.my_car.ui.theme.My_carTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {

    private lateinit var repository: MediaStoreRepository
    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaController: MediaControllerCompat? = null
    private var mediaPlayer: MediaPlayer? = null

    private val audioTracksState = mutableStateListOf<MediaTrack>()
    private val videoTracksState = mutableStateListOf<MediaTrack>()
    private val photoTracksState = mutableStateListOf<MediaTrack>()
    private val currentTrackState = mutableStateOf<MediaTrack?>(null)
    private val isPlayingState = mutableStateOf(false)
    private val isShuffleState = mutableStateOf(false)
    private val isRepeatOneState = mutableStateOf(false)
    private val currentPositionMsState = mutableLongStateOf(0L)
    private val durationMsState = mutableLongStateOf(0L)
    private val hasPermissionState = mutableStateOf(false)

    // ⚡ REACTIVE TRIGGER STATE: Increments on every favorite toggle for 100% instant UI updates across all cards!
    private val favoriteVersionState = mutableIntStateOf(0)

    private var hasAttemptedPermissionRequest = false

    private val notificationActionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MyMusicService.ACTION_NEXT -> playNextTrack()
                MyMusicService.ACTION_PREV -> playPrevTrack()
                MyMusicService.ACTION_NOTIF_PLAY -> pauseOrResumePlayback(forcePlay = true)
                MyMusicService.ACTION_NOTIF_PAUSE -> pauseOrResumePlayback(forcePlay = false)
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        hasPermissionState.value = granted
        if (granted) {
            loadMediaFiles()
            Toast.makeText(this, "تم منح الصلاحيات بنجاح! 🚗", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "تم رفض الصلاحية. اضغط مجدداً للانتقال للإعدادات لتمكينها يدوياً.", Toast.LENGTH_LONG).show()
        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            runOnUiThread {
                isPlayingState.value = state?.state == PlaybackStateCompat.STATE_PLAYING
            }
        }
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            runOnUiThread {
                try {
                    val controller = MediaControllerCompat(this@MainActivity, mediaBrowser.sessionToken)
                    MediaControllerCompat.setMediaController(this@MainActivity, controller)
                    mediaController = controller
                    controller.registerCallback(controllerCallback)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = MediaStoreRepository(this)

        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MyMusicService::class.java),
            connectionCallbacks,
            null
        )

        val intentFilter = IntentFilter().apply {
            addAction(MyMusicService.ACTION_NEXT)
            addAction(MyMusicService.ACTION_PREV)
            addAction(MyMusicService.ACTION_NOTIF_PLAY)
            addAction(MyMusicService.ACTION_NOTIF_PAUSE)
        }
        ContextCompat.registerReceiver(this, notificationActionReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)

        checkPermissions(autoLaunchSystemSettingsIfDenied = false)

        setContent {
            My_carTheme {
                val currentScreen = remember { mutableStateOf<CarScreen>(CarScreen.Dashboard) }
                var showExitDialog by remember { mutableStateOf(false) }

                // Intercept System Back Gesture/Button across all screens
                BackHandler {
                    if (currentScreen.value != CarScreen.Dashboard) {
                        currentScreen.value = CarScreen.Dashboard
                    } else {
                        showExitDialog = true
                    }
                }

                // High Contrast Automotive Exit Confirmation Dialog
                if (showExitDialog) {
                    AlertDialog(
                        onDismissRequest = { showExitDialog = false },
                        title = {
                            Text(
                                text = " الخروج",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        text = {
                            Text(
                                text = "هل ترغب حقاً في الخروج من مشغل وسائط السيارة؟ (يمكنك ترك المشغل يعمل بالخلفية مع الإشعار الثابت)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showExitDialog = false
                                    finish()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF5252),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("تأكيد الخروج 🚪", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = { showExitDialog = false },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("الغاء", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                // Live Position Updates Loop
                LaunchedEffect(isPlayingState.value) {
                    while (isPlayingState.value) {
                        mediaPlayer?.let { player ->
                            if (player.isPlaying) {
                                currentPositionMsState.longValue = player.currentPosition.toLong()
                                durationMsState.longValue = player.duration.coerceAtLeast(1).toLong()
                            }
                        }
                        delay(250.milliseconds)
                    }
                }

                AutomotiveGestureFrame(isPlaying = isPlayingState.value) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        if (!hasPermissionState.value) {
                            PermissionScreen(
                                onRequestPermission = { checkPermissions(autoLaunchSystemSettingsIfDenied = true) },
                                onOpenSettings = { openAppSettings(this@MainActivity) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        } else {
                            when (currentScreen.value) {
                                CarScreen.Dashboard -> {
                                    DashboardScreen(
                                        audioTracksCount = audioTracksState.size,
                                        videoTracksCount = videoTracksState.size,
                                        favoritesCount = audioTracksState.count { it.isFavorite } + videoTracksState.count { it.isFavorite } + photoTracksState.count { it.isFavorite },
                                        onNavigate = { screen -> currentScreen.value = screen },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }

                                CarScreen.Audio -> {
                                    AudioPlayerScreen(
                                        tracks = audioTracksState,
                                        favoriteVersion = favoriteVersionState.intValue,
                                        currentTrack = currentTrackState.value,
                                        isPlaying = isPlayingState.value,
                                        currentPositionMs = currentPositionMsState.longValue,
                                        durationMs = durationMsState.longValue,
                                        isShuffle = isShuffleState.value,
                                        isRepeatOne = isRepeatOneState.value,
                                        onBack = { currentScreen.value = CarScreen.Dashboard },
                                        onTrackSelect = { track -> playTrack(track) },
                                        onPlayPauseClick = { togglePlayPause() },
                                        onNextClick = { playNextTrack() },
                                        onPrevClick = { playPrevTrack() },
                                        onSeekTo = { pos -> seekToPosition(pos) },
                                        onToggleShuffle = { isShuffleState.value = !isShuffleState.value },
                                        onToggleRepeat = { isRepeatOneState.value = !isRepeatOneState.value },
                                        onToggleFavorite = { track -> toggleFavorite(track) },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }

                                CarScreen.Video -> {
                                    VideoPlayerScreen(
                                        videos = videoTracksState,
                                        photos = photoTracksState,
                                        hasPhotoPermission = checkPhotoPermission(this@MainActivity),
                                        onRequestPhotoPermission = { checkPermissions(autoLaunchSystemSettingsIfDenied = true) },
                                        onBack = { currentScreen.value = CarScreen.Dashboard },
                                        onToggleFavorite = { track -> toggleFavorite(track) },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }

                                CarScreen.Folders -> {
                                    FoldersScreen(
                                        tracks = audioTracksState,
                                        onBack = { currentScreen.value = CarScreen.Dashboard },
                                        onTrackSelect = { track -> playTrack(track) },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }

                                CarScreen.Favorites -> {
                                    FavoritesScreen(
                                        tracks = audioTracksState + videoTracksState + photoTracksState,
                                        favoriteVersion = favoriteVersionState.intValue,
                                        onBack = { currentScreen.value = CarScreen.Dashboard },
                                        onTrackSelect = { track -> playTrack(track) },
                                        onToggleFavorite = { track -> toggleFavorite(track) },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }

                                CarScreen.Settings -> {
                                    SettingsScreen(
                                        hasMediaPermission = hasPermissionState.value,
                                        onRequestMediaPermission = { checkPermissions(autoLaunchSystemSettingsIfDenied = true) },
                                        onBack = { currentScreen.value = CarScreen.Dashboard },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Silent check on return from System Settings
        val permissions = getRequiredPermissions()
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted && !hasPermissionState.value) {
            hasPermissionState.value = true
            loadMediaFiles()
        }
    }

    private fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return permissions
    }

    private fun checkPermissions(autoLaunchSystemSettingsIfDenied: Boolean = false) {
        val permissions = getRequiredPermissions()
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        hasPermissionState.value = allGranted
        if (allGranted) {
            loadMediaFiles()
            return
        }

        if (autoLaunchSystemSettingsIfDenied) {
            val shouldShowRationale = permissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(this, it)
            }

            if (hasAttemptedPermissionRequest && !shouldShowRationale) {
                // Denied or "Don't Ask Again" checked -> Directly open System App Settings
                openAppSettings(this)
                return
            }

            hasAttemptedPermissionRequest = true
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun toggleFavorite(track: MediaTrack) {
        if (track.isImage) {
            val index = photoTracksState.indexOfFirst { it.id == track.id }
            if (index != -1) {
                photoTracksState[index] = track.copy(isFavorite = !track.isFavorite)
                favoriteVersionState.intValue += 1
            }
        } else if (track.isVideo) {
            val index = videoTracksState.indexOfFirst { it.id == track.id }
            if (index != -1) {
                videoTracksState[index] = track.copy(isFavorite = !track.isFavorite)
                favoriteVersionState.intValue += 1
            }
        } else {
            val index = audioTracksState.indexOfFirst { it.id == track.id }
            if (index != -1) {
                val updated = track.copy(isFavorite = !track.isFavorite)
                audioTracksState[index] = updated
                if (currentTrackState.value?.id == track.id) {
                    currentTrackState.value = updated
                }
                favoriteVersionState.intValue += 1
                MediaIndexCache.updateAudioIndex(audioTracksState.toList())
            }
        }
    }

    private fun loadMediaFiles() {
        lifecycleScope.launch {
            val audio = repository.loadAudioTracks()
            val video = repository.loadVideoTracks()
            val photo = repository.loadPhotoTracks()

            audioTracksState.clear()
            audioTracksState.addAll(audio)

            videoTracksState.clear()
            videoTracksState.addAll(video)

            photoTracksState.clear()
            photoTracksState.addAll(photo)

            if (currentTrackState.value == null && audio.isNotEmpty()) {
                currentTrackState.value = audio.first()
            }
        }
    }

    private fun playTrack(track: MediaTrack) {
        currentTrackState.value = track
        try {
            val serviceIntent = Intent(this, MyMusicService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)

            mediaPlayer?.stop()
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(this@MainActivity, track.uri)
                prepare()
                start()

                setOnCompletionListener {
                    playNextTrack()
                }
            }
            isPlayingState.value = true
            durationMsState.longValue = track.durationMs.coerceAtLeast(1L)
            currentPositionMsState.longValue = 0L

            val extras = Bundle().apply {
                putString("title", track.title)
                putString("artist", track.artist)
                putLong("durationMs", track.durationMs)
                putLong("positionMs", 0L)
            }
            mediaController?.transportControls?.playFromMediaId(track.id.toString(), extras)
        } catch (e: Exception) {
            e.printStackTrace()
            isPlayingState.value = false
        }
    }

    private fun pauseOrResumePlayback(forcePlay: Boolean) {
        val player = mediaPlayer ?: return
        if (forcePlay) {
            if (!player.isPlaying) {
                player.start()
                isPlayingState.value = true
            }
        } else {
            if (player.isPlaying) {
                player.pause()
                isPlayingState.value = false
            }
        }
    }

    private fun togglePlayPause() {
        val player = mediaPlayer ?: run {
            currentTrackState.value?.let { playTrack(it) }
            return
        }

        if (player.isPlaying) {
            player.pause()
            isPlayingState.value = false
            mediaController?.transportControls?.pause()
        } else {
            player.start()
            isPlayingState.value = true
            mediaController?.transportControls?.play()
        }
    }

    private fun seekToPosition(positionMs: Long) {
        mediaPlayer?.let { player ->
            try {
                val clampedPos = positionMs.coerceIn(0L, durationMsState.longValue)
                player.seekTo(clampedPos.toInt())
                currentPositionMsState.longValue = clampedPos
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playNextTrack() {
        if (audioTracksState.isEmpty()) return
        val current = currentTrackState.value

        if (isRepeatOneState.value && current != null) {
            playTrack(current)
            return
        }

        if (isShuffleState.value) {
            val randomIndex = audioTracksState.indices.random()
            playTrack(audioTracksState[randomIndex])
            return
        }

        val index = if (current != null) audioTracksState.indexOfFirst { it.id == current.id } else -1
        val nextIndex = if (index != -1 && index + 1 < audioTracksState.size) index + 1 else 0
        playTrack(audioTracksState[nextIndex])
    }

    private fun playPrevTrack() {
        if (audioTracksState.isEmpty()) return
        val current = currentTrackState.value
        val index = if (current != null) audioTracksState.indexOfFirst { it.id == current.id } else -1
        val prevIndex = if (index > 0) index - 1 else audioTracksState.size - 1
        playTrack(audioTracksState[prevIndex])
    }

    override fun onStart() {
        super.onStart()
        try {
            if (!mediaBrowser.isConnected) {
                mediaBrowser.connect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(notificationActionReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
