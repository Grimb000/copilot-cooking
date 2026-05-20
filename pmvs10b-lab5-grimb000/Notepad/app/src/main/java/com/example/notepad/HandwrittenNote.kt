package com.example.notepad

data class StrokePoint(
    val x: Float,
    val y: Float,
    val timestamp: Long
)

data class StrokeData(
    val points: List<StrokePoint>
)

data class HandwrittenNote(
    val text: String,
    val strokes: List<StrokeData>
)
