package com.example.notepad

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.Ink

class DigitalInkHelper {

    private val modelIdentifier = requireNotNull(
        DigitalInkRecognitionModelIdentifier.fromLanguageTag(LANGUAGE_TAG)
    ) {
        "Russian digital ink model is not available."
    }
    private val model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
    private val recognizer = DigitalInkRecognition.getClient(
        DigitalInkRecognizerOptions.builder(model).build()
    )
    private val remoteModelManager = RemoteModelManager.getInstance()

    fun checkModelAvailability(
        onSuccess: (Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        remoteModelManager.isModelDownloaded(model)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    fun downloadModel(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        remoteModelManager.download(model, DownloadConditions.Builder().build())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun recognize(
        strokes: List<StrokeData>,
        onSuccess: (String?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        recognizer.recognize(strokes.toInk())
            .addOnSuccessListener { result ->
                onSuccess(result.candidates.firstOrNull()?.text)
            }
            .addOnFailureListener(onFailure)
    }

    fun close() {
        recognizer.close()
    }

    private fun List<StrokeData>.toInk(): Ink {
        val firstTimestamp = asSequence()
            .flatMap { it.points.asSequence() }
            .firstOrNull()
            ?.timestamp
            ?: 0L

        val inkBuilder = Ink.builder()
        forEach { stroke ->
            if (stroke.points.isEmpty()) {
                return@forEach
            }
            val strokeBuilder = Ink.Stroke.builder()
            stroke.points.forEach { point ->
                strokeBuilder.addPoint(
                    Ink.Point.create(
                        point.x,
                        point.y,
                        (point.timestamp - firstTimestamp).coerceAtLeast(0L)
                    )
                )
            }
            inkBuilder.addStroke(strokeBuilder.build())
        }
        return inkBuilder.build()
    }

    private companion object {
        const val LANGUAGE_TAG = "ru"
    }
}
