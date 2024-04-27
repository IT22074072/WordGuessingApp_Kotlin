package com.droidbytes.wordguessinggame

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import androidx.core.widget.doOnTextChanged
import com.droidbytes.wordguessinggame.databinding.ActivityGameBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GameActivity : AppCompatActivity() {
    // Declare variables
    private lateinit var binding: ActivityGameBinding
    private var questionCount: Int = 0
    private var answer: String = ""
    private var correct: Int = 0
    private var currentAnswer: String = ""
    private lateinit var questionList: ArrayList<String>
    private lateinit var answerList: ArrayList<String>
    private var isButtonClickable: Boolean = true
    private lateinit var questionListOld: ArrayList<String>
    private lateinit var answerListOld: ArrayList<String>
    private lateinit var clickMediaPlayer: MediaPlayer
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var hashMap: HashMap<String, String>
    private var remaining: Int = 0
    private lateinit var questionRef: DatabaseReference
    private lateinit var assetManager: AssetManager
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize views
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database reference
        questionRef = FirebaseDatabase.getInstance().reference.child("question")


        // Initialize ArrayLists and HashMap
        questionList = ArrayList()
        answerList = ArrayList()
        questionListOld = ArrayList()
        answerListOld = ArrayList()
        hashMap = HashMap()


        // Retrieve intent extras and asset manager
        questionListOld = intent.getStringArrayListExtra("questions") as ArrayList
        answerListOld = intent.getStringArrayListExtra("answers") as ArrayList
        assetManager = applicationContext.assets


        // Initialize SharedPreferences
        sharedPreferences = applicationContext.getSharedPreferences("score", MODE_PRIVATE)  //SharedPreferences file can only be accessed by the calling application.
        editor = sharedPreferences.edit()


        // Retrieve and display best score
        val bestScore = sharedPreferences.getString("bestScore", "0")
        binding.bestScore.text = "Your Best Score : $bestScore"


        // Populate HashMap and shuffle entries
        for (each in 0 until questionListOld.size) {
            hashMap[questionListOld[each]] = answerListOld[each]
        }

        val entries = hashMap.entries.shuffled()


        // Initialize timer
        timer()


        // Populate questionList and answerList
        for (each in entries) {
            questionList.add(each.key)
            answerList.add(each.value)
        }

        remaining = 15


        // Update UI with initial question and answer fields
        updateAnswerAndQuestion()


        // Handle EditText focus changes
        binding.et1.doOnTextChanged { _, _, _, _ ->

            // Move focus to the next EditText if current one is filled
            if (binding.et1.text.toString().isNotEmpty()) {
                binding.et2.requestFocus()
            }
            if (binding.et2.text.toString().isNotEmpty()) {
                binding.et2.requestFocus()
            }
        }


        // Similar logic for the remaining EditText fields
        // (et2, et3, et4, et5, et6)

        binding.et2.doOnTextChanged { _, _, _, _ ->
            if (binding.et2.text.toString().isNotEmpty()) {
                binding.et3.requestFocus()
            } else {
                binding.et1.requestFocus()
            }

        }
        binding.et3.doOnTextChanged { _, _, _, _ ->
            if (binding.et3.text.toString().isNotEmpty()) {
                binding.et4.requestFocus()
            } else {
                binding.et2.requestFocus()
            }

        }
        binding.et4.doOnTextChanged { _, _, _, _ ->
            if (binding.et4.text.toString().isNotEmpty()) {
                binding.et5.requestFocus()
            } else {
                binding.et3.requestFocus()
            }

        }
        binding.et5.doOnTextChanged { _, _, _, _ ->
            if (binding.et5.text.toString().isNotEmpty()) {
                binding.et6.requestFocus()
            } else {
                binding.et4.requestFocus()
            }

        }
        binding.et6.doOnTextChanged { _, _, _, _ ->
            if (binding.et6.text.toString().isEmpty()) {
                binding.et5.requestFocus()
            }
        }



        // Handle click on next button
        binding.nextbtn.setOnClickListener {
            // Play click sound
            val musicFileName = "clickButton.mp3"
            val descriptor: AssetFileDescriptor = assetManager.openFd(musicFileName)
            clickMediaPlayer = MediaPlayer()
            clickMediaPlayer.setDataSource(
                descriptor.fileDescriptor,
                descriptor.startOffset,
                descriptor.length
            )

            clickMediaPlayer.prepare()
            clickMediaPlayer.start()


            // Concatenate user input to form the answer
            answer = binding.et1.text.toString() +
                    binding.et2.text.toString() +
                    binding.et3.text.toString() +
                    binding.et4.text.toString() +
                    binding.et5.text.toString() +
                    binding.et6.text.toString()


            // Check if it's the last question
            if (remaining == 1) {
                // If it's the last question, check if the current answer is correct
                if (currentAnswer.equals(answer, ignoreCase = true)) {
                    // Increment correct count if the answer is correct
                    correct++
                    // Update UI with correct count
                    binding.solvedtv.text = correct.toString()



                    // Prepare intent to start ResultActivity
                    val intent = Intent(this@GameActivity, ResultActivity::class.java)
                    intent.putExtra("result", correct.toString())
                    intent.putExtra("questions", questionList)
                    intent.putExtra("answers", answerList)

                    // Start ResultActivity
                    startActivity(intent)
                    // Finish current activity
                    finish()
                } else {
                    // If the answer is incorrect for the last question
                    val intent = Intent(this@GameActivity, ResultActivity::class.java)
                    intent.putExtra("result", correct.toString())
                    intent.putExtra("questions", questionList)
                    intent.putExtra("answers", answerList)
                    startActivity(intent)
                    finish()
                }
            } else {
                // If it's not the last question
                if (isButtonClickable && currentAnswer.equals(answer, ignoreCase = true)) {
                    // If the button is clickable and the answer is correct
                    isButtonClickable = false

                    // Cancel the countdown timer
                    countDownTimer.cancel()

                    // Show correct animation
                    binding.lottie.visibility = View.VISIBLE
                    binding.lottie.setAnimation("correct.json")
                    binding.lottie.playAnimation()

                    val handler = Handler()
                    handler.postDelayed({

                        binding.lottie.visibility = View.GONE

                        // Decrement remaining attempts
                        remaining -= 1

                        correct++
                        binding.solvedtv.setText(correct.toString())

                        // Set button clickable again
                        isButtonClickable = true

                        countDownTimer.cancel()

                        // Update question and timer
                        updateAnswerAndQuestion()
                        timer()
                    }, 2000)

                } else {
                    // If the button is clickable and the answer is incorrect
                    if (isButtonClickable) {
                        // Set button clickable to false
                        isButtonClickable = false

                        // Cancel countdown timer
                        countDownTimer.cancel()

                        // Show wrong animation
                        binding.lottie.visibility = View.VISIBLE
                        binding.lottie.setAnimation("wrong.json")
                        binding.lottie.playAnimation()

                        val handler = Handler()
                        handler.postDelayed({
                            isButtonClickable = true
                            binding.lottie.visibility = View.GONE

                            remaining -= 1

                            countDownTimer.cancel()

                            timer()

                            updateAnswerAndQuestion()
                        }, 2000)
                    }
                }
            }
        }
    }

    fun updateAnswerAndQuestion() {
        // Update remaining attempts count
        binding.remainingTv.text = remaining.toString()
        // Display current question
        binding.hintTv.text = questionList[questionCount].uppercase(Locale.getDefault())

        // Retrieve current answer
        currentAnswer = answerList[questionCount]

        // Move to the next question and handle overflow
        questionCount += 1
        if (questionCount >= questionList.size) {
            questionCount = 0
        }

        // Show/hide EditText fields based on answer length
        when (currentAnswer.length) {
            1 -> {
                binding.et1.visibility = View.VISIBLE
                binding.et2.visibility = View.GONE
                binding.et3.visibility = View.GONE
                binding.et4.visibility = View.GONE
                binding.et5.visibility = View.GONE
                binding.et6.visibility = View.GONE
            }

            2 -> {
                binding.et1.visibility = View.VISIBLE
                binding.et2.visibility = View.VISIBLE
                binding.et3.visibility = View.GONE
                binding.et4.visibility = View.GONE
                binding.et5.visibility = View.GONE
                binding.et6.visibility = View.GONE
            }

            3 -> {
                binding.et1.visibility = View.VISIBLE
                binding.et2.visibility = View.VISIBLE
                binding.et3.visibility = View.VISIBLE
                binding.et4.visibility = View.GONE
                binding.et5.visibility = View.GONE
                binding.et6.visibility = View.GONE
            }

            4 -> {
                binding.et1.visibility = View.VISIBLE
                binding.et2.visibility = View.VISIBLE
                binding.et3.visibility = View.VISIBLE
                binding.et4.visibility = View.VISIBLE
                binding.et5.visibility = View.GONE
                binding.et6.visibility = View.GONE
            }

            5 -> {
                binding.et1.visibility = View.VISIBLE
                binding.et2.visibility = View.VISIBLE
                binding.et3.visibility = View.VISIBLE
                binding.et4.visibility = View.VISIBLE
                binding.et5.visibility = View.VISIBLE
                binding.et6.visibility = View.GONE
            }

            6 -> {
                binding.et1.visibility = View.VISIBLE
                binding.et2.visibility = View.VISIBLE
                binding.et3.visibility = View.VISIBLE
                binding.et4.visibility = View.VISIBLE
                binding.et5.visibility = View.VISIBLE
                binding.et6.visibility = View.VISIBLE
            }
        }
        // Clear EditText fields and set focus to the first EditText
        binding.et1.text.clear()
        binding.et2.text.clear()
        binding.et3.text.clear()
        binding.et4.text.clear()
        binding.et5.text.clear()
        binding.et6.text.clear()
        binding.et1.requestFocus()
    }


    fun timer() {
        // Initialize countdown timer
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update timer text with remaining seconds
                val secondsLeft = millisUntilFinished / 1000
                binding.timerTv.text = secondsLeft.toString()
            }

            override fun onFinish() {
                // When timer finishes
                if (remaining == 1) {
                    // If it's the last attempt, start ResultActivity
                    val intent = Intent(this@GameActivity, ResultActivity::class.java)
                    intent.putExtra("result", correct.toString())
                    intent.putExtra("questions", questionList)
                    intent.putExtra("answers", answerList)
                    startActivity(intent)
                    finish()
                } else {
                    // If it's not the last attempt, show wrong animation
                    binding.lottie.visibility = View.VISIBLE
                    binding.lottie.setAnimation("wrong.json")
                    binding.lottie.playAnimation()

                    val handler = Handler()
                    handler.postDelayed({
                        // Hide the Lottie animation view
                        binding.lottie.visibility = View.GONE
                        // Decrement remaining attempts
                        remaining -= 1
                        // Restart timer and update question
                        timer()
                        updateAnswerAndQuestion()
                    }, 2000)
                }
            }
        }
        countDownTimer.start()
    }

    // Handle back button press
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()

        // Play click sound on back button press
        val musicFileName = "clickButton.mp3"
        val descriptor: AssetFileDescriptor = assetManager.openFd(musicFileName)

        clickMediaPlayer = MediaPlayer()
        clickMediaPlayer.setDataSource(
            descriptor.fileDescriptor,
            descriptor.startOffset,
            descriptor.length
        )
        clickMediaPlayer.prepare()
        clickMediaPlayer.start()
    }
}