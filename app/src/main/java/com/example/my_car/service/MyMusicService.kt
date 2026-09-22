package com.example.my_car.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.example.my_car.MainActivity
import com.example.my_car.R
import androidx.core.graphics.createBitmap

@Suppress("DEPRECATION")
class MyMusicService : MediaBrowserServiceCompat() {

    private lateinit var session: MediaSessionCompat
    private var lastPositionMs: Long = 0L
    private var isMuted: Boolean = false
    private var savedVolume: Int = 0

    companion object {
        const val CHANNEL_ID = "my_car_media_channel_v4"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.example.my_car.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.my_car.ACTION_PAUSE"
        const val ACTION_NOTIF_PLAY = "com.example.my_car.ACTION_NOTIF_PLAY"
        const val ACTION_NOTIF_PAUSE = "com.example.my_car.ACTION_NOTIF_PAUSE"
        const val ACTION_NEXT = "com.example.my_car.ACTION_NEXT"
        const val ACTION_PREV = "com.example.my_car.ACTION_PREV"
        const val ACTION_MUTE_TOGGLE = "com.example.my_car.ACTION_MUTE_TOGGLE"
        const val ACTION_STOP = "com.example.my_car.ACTION_STOP"
    }

    private val callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            setPlaybackState(PlaybackStateCompat.STATE_PLAYING, positionMs = lastPositionMs)
            updateNotification(isPlaying = true)
        }

        override fun onPause() {
            setPlaybackState(PlaybackStateCompat.STATE_PAUSED, positionMs = lastPositionMs)
            updateNotification(isPlaying = false)
        }

        override fun onStop() {
            setPlaybackState(PlaybackStateCompat.STATE_STOPPED, positionMs = 0L)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        }

        override fun onSkipToNext() {
            sendBroadcast(Intent(ACTION_NEXT).apply { setPackage(packageName) })
        }

        override fun onSkipToPrevious() {
            sendBroadcast(Intent(ACTION_PREV).apply { setPackage(packageName) })
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            val title = extras?.getString("title") ?: "مشغل وسائط السيارة"
            val artist = extras?.getString("artist") ?: "الوسائط المحلية"
            val durationMs = extras?.getLong("durationMs") ?: 0L
            val positionMs = extras?.getLong("positionMs") ?: 0L
            lastPositionMs = positionMs

            val placeholderArt = createCyberArtworkBitmap(title)

            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, placeholderArt)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, placeholderArt)
                .build()

            session.setMetadata(metadata)
            setPlaybackState(PlaybackStateCompat.STATE_PLAYING, positionMs = positionMs)
            updateNotification(title = title, artist = artist, isPlaying = true, artBitmap = placeholderArt)
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        session = MediaSessionCompat(this, "MyMusicService").apply {
            setFlags(
                FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setCallback(callback)
            isActive = true
        }

        sessionToken = session.sessionToken
        setPlaybackState(PlaybackStateCompat.STATE_PAUSED, positionMs = 0L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(session, intent)

        when (intent?.action) {
            ACTION_PLAY -> {
                sendBroadcast(Intent(ACTION_NOTIF_PLAY).apply { setPackage(packageName) })
                callback.onPlay()
            }
            ACTION_PAUSE -> {
                sendBroadcast(Intent(ACTION_NOTIF_PAUSE).apply { setPackage(packageName) })
                callback.onPause()
            }
            ACTION_NEXT -> callback.onSkipToNext()
            ACTION_PREV -> callback.onSkipToPrevious()
            ACTION_MUTE_TOGGLE -> toggleMute()
            ACTION_STOP -> callback.onStop()
        }

        return START_STICKY
    }

    private fun toggleMute() {
        val audioManager = getSystemService(AUDIO_SERVICE) as? AudioManager ?: return
        if (isMuted) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, savedVolume.coerceAtLeast(1), 0)
            isMuted = false
        } else {
            savedVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            isMuted = true
        }
        val isPlaying = session.controller.playbackState?.state == PlaybackStateCompat.STATE_PLAYING
        updateNotification(isPlaying = isPlaying)
    }

    private fun setPlaybackState(state: Int, positionMs: Long) {
        lastPositionMs = positionMs
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_STOP
            )
            .setState(state, positionMs, if (state == PlaybackStateCompat.STATE_PLAYING) 1.0f else 0.0f)
            .build()

        session.setPlaybackState(playbackState)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "مشغل وسائط السيارة التفاعلي",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعارات وسائط السيارة المضيئة والمثبتة دائمياً"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createCyberArtworkBitmap(title: String): Bitmap {
        val bitmap = createBitmap(160, 160)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.parseColor("#0284C7")
            isAntiAlias = true
        }
        canvas.drawCircle(80f, 80f, 75f, paint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 64f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        val firstChar = if (title.isNotEmpty()) title.substring(0, 1) else "🎵"
        canvas.drawText(firstChar, 80f, 102f, textPaint)
        return bitmap
    }

    private fun updateNotification(
        title: String = session.controller.metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: "مشغل الوسائط للسيارة",
        artist: String = session.controller.metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST) ?: "الوسائط المحلية",
        isPlaying: Boolean,
        artBitmap: Bitmap? = session.controller.metadata?.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevPendingIntent = PendingIntent.getService(
            this, 1, Intent(this, MyMusicService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        val playPausePendingIntent = PendingIntent.getService(
            this, 2, Intent(this, MyMusicService::class.java).apply { action = playPauseAction },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextPendingIntent = PendingIntent.getService(
            this, 3, Intent(this, MyMusicService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mutePendingIntent = PendingIntent.getService(
            this, 4, Intent(this, MyMusicService::class.java).apply { action = ACTION_MUTE_TOGGLE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(if (isMuted) "🔇 كتم الصوت مفعّل" else "وسائط السيارة 🎵")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .addAction(
                android.R.drawable.ic_media_previous,
                "السابق",
                prevPendingIntent
            )
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "إيقاف" else "تشغيل",
                playPausePendingIntent
            )
            .addAction(
                android.R.drawable.ic_media_next,
                "التالي",
                nextPendingIntent
            )
            .addAction(
                android.R.drawable.ic_lock_silent_mode,
                if (isMuted) "إلغاء الكتم" else "كتم",
                mutePendingIntent
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (artBitmap != null) {
            notificationBuilder.setLargeIcon(artBitmap)
        }

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onDestroy() {
        session.isActive = false
        session.release()
        super.onDestroy()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaItem>>
    ) {
        result.sendResult(mutableListOf())
    }
}
