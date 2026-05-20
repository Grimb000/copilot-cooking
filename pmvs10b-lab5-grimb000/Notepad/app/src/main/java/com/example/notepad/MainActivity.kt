package com.example.notepad

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var modelStatusTextView: TextView
    private lateinit var downloadModelButton: MaterialButton
    private lateinit var inkCanvasView: InkCanvasView
    private lateinit var recognizedEditText: TextInputEditText
    private lateinit var recognizeButton: MaterialButton
    private lateinit var clearCanvasButton: MaterialButton
    private lateinit var saveButton: MaterialButton

    private lateinit var storage: NotepadStorage
    private lateinit var digitalInkHelper: DigitalInkHelper

    private var isModelReady = false
    private var isDownloadingModel = false
    private var isRecognizing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        storage = NotepadStorage(this)
        digitalInkHelper = DigitalInkHelper()

        bindViews()
        restoreSavedNote()
        bindActions()
        refreshModelStatus()
    }

    override fun onDestroy() {
        digitalInkHelper.close()
        super.onDestroy()
    }

    private fun bindViews() {
        modelStatusTextView = findViewById(R.id.modelStatusTextView)
        downloadModelButton = findViewById(R.id.downloadModelButton)
        inkCanvasView = findViewById(R.id.inkCanvasView)
        recognizedEditText = findViewById(R.id.recognizedEditText)
        recognizeButton = findViewById(R.id.recognizeButton)
        clearCanvasButton = findViewById(R.id.clearCanvasButton)
        saveButton = findViewById(R.id.saveButton)
    }

    private fun restoreSavedNote() {
        val note = storage.loadNote()
        inkCanvasView.setStrokes(note.strokes)
        recognizedEditText.setText(note.text)
    }

    private fun bindActions() {
        downloadModelButton.setOnClickListener {
            downloadModel()
        }
        recognizeButton.setOnClickListener {
            recognizeInk()
        }
        clearCanvasButton.setOnClickListener {
            clearCanvas()
        }
        saveButton.setOnClickListener {
            saveNote()
        }
    }

    private fun refreshModelStatus() {
        modelStatusTextView.setText(R.string.model_status_unknown)
        digitalInkHelper.checkModelAvailability(
            onSuccess = { downloaded ->
                isModelReady = downloaded
                modelStatusTextView.setText(
                    if (downloaded) {
                        R.string.model_status_ready
                    } else {
                        R.string.model_status_missing
                    }
                )
                updateButtonsState()
            },
            onFailure = {
                isModelReady = false
                modelStatusTextView.setText(R.string.model_status_missing)
                updateButtonsState()
            }
        )
    }

    private fun downloadModel() {
        if (isDownloadingModel) {
            return
        }
        isDownloadingModel = true
        modelStatusTextView.setText(R.string.model_status_downloading)
        updateButtonsState()

        digitalInkHelper.downloadModel(
            onSuccess = {
                isDownloadingModel = false
                isModelReady = true
                modelStatusTextView.setText(R.string.model_status_ready)
                updateButtonsState()
                showToast(R.string.download_success_message)
            },
            onFailure = {
                isDownloadingModel = false
                isModelReady = false
                modelStatusTextView.setText(R.string.model_status_download_failed)
                updateButtonsState()
                showToast(R.string.download_failed_message)
            }
        )
    }

    private fun recognizeInk() {
        if (isRecognizing) {
            return
        }
        if (!inkCanvasView.hasInk()) {
            showToast(R.string.empty_canvas_message)
            return
        }
        if (!isModelReady) {
            showToast(R.string.model_not_ready_message)
            return
        }

        isRecognizing = true
        updateButtonsState()

        digitalInkHelper.recognize(
            strokes = inkCanvasView.getStrokes(),
            onSuccess = { recognizedText ->
                isRecognizing = false
                updateButtonsState()
                if (recognizedText.isNullOrBlank()) {
                    showToast(R.string.recognition_empty_result)
                    return@recognize
                }
                appendRecognizedText(recognizedText)
            },
            onFailure = {
                isRecognizing = false
                updateButtonsState()
                showToast(R.string.recognition_failed_message)
            }
        )
    }

    private fun clearCanvas() {
        inkCanvasView.clearInk()
        showToast(R.string.canvas_cleared_message)
    }

    private fun appendRecognizedText(recognizedText: String) {
        val currentText = recognizedEditText.text?.toString().orEmpty().trim()
        val updatedText = if (currentText.isEmpty()) {
            recognizedText
        } else {
            "$currentText $recognizedText"
        }
        recognizedEditText.setText(updatedText)
        recognizedEditText.setSelection(updatedText.length)
    }

    private fun saveNote() {
        storage.saveNote(
            HandwrittenNote(
                text = recognizedEditText.text?.toString().orEmpty(),
                strokes = inkCanvasView.getStrokes()
            )
        )
        showToast(R.string.note_saved_message)
    }

    private fun updateButtonsState() {
        downloadModelButton.isEnabled = !isDownloadingModel
        recognizeButton.isEnabled = !isRecognizing
        clearCanvasButton.isEnabled = !isRecognizing
        saveButton.isEnabled = !isRecognizing
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
    }
}
