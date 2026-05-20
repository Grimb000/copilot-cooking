package com.example.guessnumber_kotlin

import android.gesture.Gesture
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.gesture.GestureOverlayView
import android.gesture.Prediction
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), GestureOverlayView.OnGesturePerformedListener {
    private var secret = 0
    private var attempts = 0
    private val maxAttempts = 10
    private var gestureBuffer = ""

    private lateinit var ivImage: ImageView
    private lateinit var tvInfo: TextView
    private lateinit var tvAttempts: TextView
    private lateinit var tvGestureBuffer: TextView
    private lateinit var etInput: EditText
    private lateinit var bCheck: Button
    private lateinit var bRestart: Button
    private lateinit var pbAttempts: ProgressBar
    private lateinit var pbLoading: ProgressBar
    private lateinit var seekBar: SeekBar
    private lateinit var tvSeekBarValue: TextView
    private lateinit var gestureOverlay: GestureOverlayView
    private lateinit var gestureStatus: TextView
    private lateinit var gestureLibrary: GestureLibrary

    private val imageUrl = "https://img.freepik.com/free-vector/guess-number-concept-illustration_114360-1234.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
        setupGestures()
        startNewGame()
        loadRemoteImage()
    }

    private fun initViews() {
        ivImage = findViewById(R.id.imageView)
        tvInfo = findViewById(R.id.textViewInfo)
        tvAttempts = findViewById(R.id.textViewAttempts)
        tvGestureBuffer = findViewById(R.id.textViewGestureBuffer)
        etInput = findViewById(R.id.editTextGuess)
        bCheck = findViewById(R.id.buttonCheck)
        bRestart = findViewById(R.id.buttonRestartMain)
        pbAttempts = findViewById(R.id.progressBarAttempts)
        pbLoading = findViewById(R.id.loadingSpinner)
        seekBar = findViewById(R.id.seekBarHelper)
        tvSeekBarValue = findViewById(R.id.textViewSeekBarValue)
        gestureOverlay = findViewById(R.id.gestureOverlay)
        gestureStatus = findViewById(R.id.textViewGestureStatus)

        pbAttempts.max = maxAttempts
    }

    private fun setupListeners() {
        bCheck.setOnClickListener { checkGuess() }
        bRestart.setOnClickListener { startNewGame() }
        etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                syncGestureBufferFromInput(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvSeekBarValue.text = progress.toString()
                if (fromUser) etInput.setText(progress.toString())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupGestures() {
        gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures)
        if (gestureLibrary.load()) {
            gestureOverlay.addOnGesturePerformedListener(this)
            gestureStatus.text = "Draw digits or use stop to submit."
        } else {
            gestureStatus.text = "Gesture library failed to load."
        }
    }

    private fun startNewGame() {
        secret = (1..100).random()
        attempts = 0
        gestureBuffer = ""
        updateAttemptsUI()
        tvInfo.text = getString(R.string.try_to_guess)
        etInput.text.clear()
        updateGestureBuffer()
        bCheck.isEnabled = true
        bRestart.visibility = View.GONE
        
        MainScope().launch {
            pbAttempts.progress = 0
            for (i in 0..maxAttempts) {
                delay(20)
                pbAttempts.progress = i
            }
        }
        
        applyAnimation(ivImage, R.anim.entrance)
    }

    private fun loadRemoteImage() {
        pbLoading.visibility = View.VISIBLE
        ivImage.load(imageUrl) {
            crossfade(true)
            transformations(CircleCropTransformation())
            listener(
                onSuccess = { _, _ -> pbLoading.visibility = View.GONE },
                onError = { _, _ -> pbLoading.visibility = View.GONE }
            )
        }
    }

    private fun checkGuess() {
        val input = etInput.text.toString().toIntOrNull()
        if (input == null || input !in 1..100) {
            tvInfo.text = getString(R.string.error)
            applyAnimation(etInput, R.anim.shake)
            return
        }

        attempts++
        updateAttemptsUI()

        when {
            input < secret -> {
                tvInfo.text = getString(R.string.behind)
                applyAnimation(tvInfo, R.anim.hint_bounce)
            }
            input > secret -> {
                tvInfo.text = getString(R.string.ahead)
                applyAnimation(tvInfo, R.anim.hint_bounce)
            }
            else -> {
                showEndDialog(true)
            }
        }

        if (attempts >= maxAttempts && input != secret) {
            showEndDialog(false)
        }

        gestureBuffer = ""
        updateGestureBuffer()
    }

    private fun updateAttemptsUI() {
        val left = maxAttempts - attempts
        tvAttempts.text = getString(R.string.attempts_left, left)
        pbAttempts.progress = left
    }

    private fun showEndDialog(isWin: Boolean) {
        bCheck.isEnabled = false
        bRestart.visibility = View.VISIBLE

        val message = if (isWin) {
            getString(R.string.win_message, secret, attempts)
        } else {
            getString(R.string.lose_message, secret)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.game_over)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
            
        if (isWin) applyAnimation(ivImage, R.anim.win_celebration)
    }

    private fun applyAnimation(view: View, animResId: Int) {
        val animation = AnimationUtils.loadAnimation(this, animResId)
        view.startAnimation(animation)
    }

    override fun onGesturePerformed(overlay: GestureOverlayView?, gesture: Gesture?) {
        if (gesture == null) return

        val bestPrediction = gestureLibrary
            .recognize(gesture)
            .maxByOrNull(Prediction::score)

        if (bestPrediction == null || bestPrediction.score <= 1.0) {
            gestureStatus.text = "Unknown gesture"
            return
        }

        handleGesture(bestPrediction.name.trim())
    }

    private fun handleGesture(name: String) {
        when {
            name.length == 1 && name[0].isDigit() -> appendGestureDigit(name)
            name.equals("stop", ignoreCase = true) || name == "=" -> {
                gestureStatus.text = "Submitting gesture input"
                checkGuess()
            }
            name.equals("c", ignoreCase = true) -> {
                gestureBuffer = ""
                etInput.text.clear()
                updateGestureBuffer()
                gestureStatus.text = "Gesture input cleared"
            }
            else -> gestureStatus.text = "Gesture '$name' is not used here"
        }
    }

    private fun appendGestureDigit(digit: String) {
        if (gestureBuffer.length >= 3) {
            gestureStatus.text = "Too many digits"
            return
        }

        gestureBuffer += digit
        etInput.setText(gestureBuffer)
        etInput.setSelection(etInput.text.length)
        updateGestureBuffer()
        gestureStatus.text = "Recognized gesture: $digit"
    }

    private fun updateGestureBuffer() {
        val shownBuffer = if (gestureBuffer.isEmpty()) "-" else gestureBuffer
        tvGestureBuffer.text = getString(R.string.gesture_buffer_value, shownBuffer)
    }

    private fun syncGestureBufferFromInput(text: String) {
        val normalized = text.filter(Char::isDigit).take(3)
        if (gestureBuffer != normalized) {
            gestureBuffer = normalized
            updateGestureBuffer()
        }
    }
}
