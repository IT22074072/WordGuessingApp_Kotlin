package com.droidbytes.wordguessinggame

import android.app.Service
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.media.MediaPlayer
import android.os.IBinder

class MusicService : Service() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var assetManager : AssetManager

    override fun onCreate() {
        super.onCreate()
        // Initialize asset manager to access music file
        assetManager = applicationContext.assets

        // Load background music file
        val musicFileName = "bgMusic.mp3"
        val descriptor: AssetFileDescriptor = assetManager.openFd(musicFileName)

        // Initialize media player and set data source
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
        // Set looping to repeat the music
        mediaPlayer.isLooping=true
        // Prepare media player
        mediaPlayer.prepare()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start playing the background music
        mediaPlayer.start()
        return START_STICKY
    }

    override fun onDestroy() {
        // Stop and release media player when service is destroyed
        mediaPlayer.stop()
        mediaPlayer.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Return null as this service does not support binding
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Stop music playback when the task is removed
        mediaPlayer.stop()
        super.onTaskRemoved(rootIntent)

    }
}