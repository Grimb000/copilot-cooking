package com.example.notepad

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

class InkCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.black)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = STROKE_WIDTH_DP * resources.displayMetrics.density
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.black)
        style = Paint.Style.FILL
    }

    private val strokes = mutableListOf<StrokeData>()
    private val currentStrokePoints = mutableListOf<StrokePoint>()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        strokes.forEach { drawStroke(canvas, it.points) }
        drawStroke(canvas, currentStrokePoints)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                currentStrokePoints.clear()
                addPoint(event.x, event.y, event.eventTime)
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                appendHistoricalPoints(event)
                addPoint(event.x, event.y, event.eventTime)
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP -> {
                appendHistoricalPoints(event)
                addPoint(event.x, event.y, event.eventTime)
                commitCurrentStroke()
                invalidate()
                performClick()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                commitCurrentStroke()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun getStrokes(): List<StrokeData> {
        return strokes.map { stroke ->
            StrokeData(stroke.points.map { point -> point.copy() })
        }
    }

    fun setStrokes(savedStrokes: List<StrokeData>) {
        strokes.clear()
        strokes.addAll(
            savedStrokes.map { stroke ->
                StrokeData(stroke.points.map { point -> point.copy() })
            }
        )
        currentStrokePoints.clear()
        invalidate()
    }

    fun clearInk() {
        strokes.clear()
        currentStrokePoints.clear()
        invalidate()
    }

    fun hasInk(): Boolean {
        return strokes.any { it.points.isNotEmpty() } || currentStrokePoints.isNotEmpty()
    }

    private fun appendHistoricalPoints(event: MotionEvent) {
        for (historyIndex in 0 until event.historySize) {
            addPoint(
                event.getHistoricalX(0, historyIndex),
                event.getHistoricalY(0, historyIndex),
                event.getHistoricalEventTime(historyIndex)
            )
        }
    }

    private fun addPoint(x: Float, y: Float, timestamp: Long) {
        val lastPoint = currentStrokePoints.lastOrNull()
        if (lastPoint != null && lastPoint.x == x && lastPoint.y == y && lastPoint.timestamp == timestamp) {
            return
        }
        currentStrokePoints.add(
            StrokePoint(
                x = x,
                y = y,
                timestamp = timestamp
            )
        )
    }

    private fun commitCurrentStroke() {
        if (currentStrokePoints.isEmpty()) {
            return
        }
        strokes.add(StrokeData(currentStrokePoints.toList()))
        currentStrokePoints.clear()
    }

    private fun drawStroke(canvas: Canvas, points: List<StrokePoint>) {
        if (points.isEmpty()) {
            return
        }
        if (points.size == 1) {
            canvas.drawCircle(
                points.first().x,
                points.first().y,
                strokePaint.strokeWidth / 2f,
                pointPaint
            )
            return
        }
        for (index in 1 until points.size) {
            val previousPoint = points[index - 1]
            val currentPoint = points[index]
            canvas.drawLine(
                previousPoint.x,
                previousPoint.y,
                currentPoint.x,
                currentPoint.y,
                strokePaint
            )
        }
    }

    private companion object {
        const val STROKE_WIDTH_DP = 4
    }
}
