package com.example.notepad

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class NotepadStorage(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun saveNote(note: HandwrittenNote) {
        preferences.edit()
            .putString(KEY_TEXT, note.text)
            .putString(KEY_STROKES, serializeStrokes(note.strokes))
            .apply()
    }

    fun loadNote(): HandwrittenNote {
        val text = preferences.getString(KEY_TEXT, "").orEmpty()
        val strokesJson = preferences.getString(KEY_STROKES, "[]").orEmpty()
        return HandwrittenNote(
            text = text,
            strokes = deserializeStrokes(strokesJson)
        )
    }

    private fun serializeStrokes(strokes: List<StrokeData>): String {
        val strokesArray = JSONArray()
        strokes.forEach { stroke ->
            val pointsArray = JSONArray()
            stroke.points.forEach { point ->
                pointsArray.put(
                    JSONObject()
                        .put(JSON_KEY_X, point.x.toDouble())
                        .put(JSON_KEY_Y, point.y.toDouble())
                        .put(JSON_KEY_TIMESTAMP, point.timestamp)
                )
            }
            strokesArray.put(
                JSONObject().put(JSON_KEY_POINTS, pointsArray)
            )
        }
        return strokesArray.toString()
    }

    private fun deserializeStrokes(rawJson: String): List<StrokeData> {
        return runCatching {
            val strokesArray = JSONArray(rawJson)
            val strokes = mutableListOf<StrokeData>()
            for (strokeIndex in 0 until strokesArray.length()) {
                val strokeObject = strokesArray.optJSONObject(strokeIndex) ?: continue
                val pointsArray = strokeObject.optJSONArray(JSON_KEY_POINTS) ?: continue
                val points = mutableListOf<StrokePoint>()
                for (pointIndex in 0 until pointsArray.length()) {
                    val pointObject = pointsArray.optJSONObject(pointIndex) ?: continue
                    points.add(
                        StrokePoint(
                            x = pointObject.optDouble(JSON_KEY_X, 0.0).toFloat(),
                            y = pointObject.optDouble(JSON_KEY_Y, 0.0).toFloat(),
                            timestamp = pointObject.optLong(JSON_KEY_TIMESTAMP, 0L)
                        )
                    )
                }
                strokes.add(StrokeData(points))
            }
            strokes
        }.getOrDefault(emptyList())
    }

    private companion object {
        const val PREFERENCES_NAME = "notepad_preferences"
        const val KEY_TEXT = "note_text"
        const val KEY_STROKES = "note_strokes"
        const val JSON_KEY_POINTS = "points"
        const val JSON_KEY_X = "x"
        const val JSON_KEY_Y = "y"
        const val JSON_KEY_TIMESTAMP = "timestamp"
    }
}
