package com.droidbytes.wordguessinggame

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.droidbytes.wordguessinggame.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var clickMediaPlayer : MediaPlayer
    private lateinit var assetManager : AssetManager
    lateinit var binding : ActivityResultBinding
    var result : String = ""
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using ViewBinding
        binding= ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize asset manager to access sound files
        assetManager=applicationContext.assets

        // Retrieve result from the intent
        result=intent.getStringExtra("result") as String

        // Initialize SharedPreferences to store the best score
        sharedPreferences=applicationContext.getSharedPreferences("score", MODE_PRIVATE)
        editor=sharedPreferences.edit()


        // Display the result
        binding.results.text= "$result/15"

        // Check if the current result is better than the best score
        if(result.toInt() > sharedPreferences.getString("bestScore","0")!!.toInt()) {
            // Update the best score in SharedPreferences
            editor.putString("bestScore", result)
            editor.commit()
            editor.apply()
        }

        // Set click listener for play again button
        binding.playAgain.setOnClickListener {
            val musicFileName = "clickButton.mp3"
            val descriptor: AssetFileDescriptor = assetManager.openFd(musicFileName)
            clickMediaPlayer = MediaPlayer()
            clickMediaPlayer.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
            clickMediaPlayer.prepare()
            clickMediaPlayer.start()

            // Start GameActivity again with the same questions and answers
            var intent=Intent(this@ResultActivity,GameActivity::class.java)
            intent.putExtra("questions",  getIntent().getStringArrayListExtra("questions") as ArrayList)
            intent.putExtra("answers", getIntent().getStringArrayListExtra("answers") as ArrayList)
            startActivity(intent)
            finish()
        }
    }

    // Handle back button press
    override fun onBackPressed() {
        super.onBackPressed()
        val musicFileName = "clickButton.mp3"
        val descriptor: AssetFileDescriptor = assetManager.openFd(musicFileName)
        clickMediaPlayer = MediaPlayer()
        clickMediaPlayer.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
        clickMediaPlayer.prepare()
        clickMediaPlayer.start()
    }
}