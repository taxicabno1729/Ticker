package com.example.liveticker.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.liveticker.R
import com.example.liveticker.data.ProbabilityPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Minimal probability line chart: gridlines at 0/50/100%, polyline,
 * gradient fill under the line. Trend up (or flat) draws in accent_green,
 * trend down in accent_red. Touch-scrubbing shows a guide line, a dot on
 * the nearest point, and a "84.7% · Jul 12" tooltip.
 */
class ProbabilityChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var points: List<ProbabilityPoint> = emptyList()
    private var scrubIndex: Int = -1

    private val density = resources.displayMetrics.density
    private val labelWidth = 32 * density
    private val chartPadding = 4 * density
    private val dateFormat = SimpleDateFormat("MMM d", Locale.US)

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.outline)
        alpha = 60
        strokeWidth = 1 * density
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.text_hint)
        textSize = 10 * density
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2 * density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.outline)
        alpha = 140
        strokeWidth = 1 * density
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dotRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.surface_1)
    }
    private val tooltipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.surface_5)
    }
    private val tooltipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.text_primary)
        textSize = 12 * density
    }

    fun setPoints(points: List<ProbabilityPoint>) {
        this.points = points.sortedBy { it.timestampMs }
        scrubIndex = -1
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (points.size < 2) return super.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                scrubIndex = ChartGeometry.nearestIndex(points, timestampAt(event.x))
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                clearScrub()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                clearScrub()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun clearScrub() {
        parent?.requestDisallowInterceptTouchEvent(false)
        scrubIndex = -1
        invalidate()
    }

    private fun timestampAt(x: Float): Long {
        val left = labelWidth
        val right = width - chartPadding
        val minT = points.first().timestampMs
        val maxT = points.last().timestampMs
        val fraction = ((x - left) / (right - left)).coerceIn(0f, 1f)
        return minT + ((maxT - minT) * fraction).toLong()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val left = labelWidth
        val right = width - chartPadding
        val top = chartPadding + labelPaint.textSize
        val bottom = height - chartPadding - labelPaint.textSize

        // Gridlines + labels at 100%, 50%, 0%
        listOf(1.0 to "100", 0.5 to "50", 0.0 to "0").forEach { (p, label) ->
            val y = yFor(p, top, bottom)
            canvas.drawLine(left, y, right, y, gridPaint)
            canvas.drawText(label, 0f, y + labelPaint.textSize / 3, labelPaint)
        }

        if (points.size < 2) return

        val color = context.getColor(
            if (ChartGeometry.trendIsUp(points)) R.color.accent_green else R.color.accent_red
        )
        linePaint.color = color
        dotPaint.color = color
        fillPaint.shader = LinearGradient(
            0f, top, 0f, bottom,
            (color and 0x00FFFFFF) or 0x55000000, (color and 0x00FFFFFF),
            Shader.TileMode.CLAMP
        )

        val minT = points.first().timestampMs
        val maxT = points.last().timestampMs
        val spanT = (maxT - minT).coerceAtLeast(1)

        val line = Path()
        points.forEachIndexed { i, pt ->
            val x = left + (right - left) * (pt.timestampMs - minT).toFloat() / spanT
            val y = yFor(pt.probability, top, bottom)
            if (i == 0) line.moveTo(x, y) else line.lineTo(x, y)
        }

        val fill = Path(line).apply {
            lineTo(right, bottom)
            lineTo(left, bottom)
            close()
        }
        canvas.drawPath(fill, fillPaint)
        canvas.drawPath(line, linePaint)

        val scrubbed = points.getOrNull(scrubIndex) ?: return
        val x = left + (right - left) * (scrubbed.timestampMs - minT).toFloat() / spanT
        val y = yFor(scrubbed.probability, top, bottom)

        canvas.drawLine(x, top, x, bottom, guidePaint)
        canvas.drawCircle(x, y, 6 * density, dotRingPaint)
        canvas.drawCircle(x, y, 4 * density, dotPaint)
        drawTooltip(canvas, scrubbed, x, left, right, top)
    }

    private fun drawTooltip(
        canvas: Canvas,
        point: ProbabilityPoint,
        anchorX: Float,
        left: Float,
        right: Float,
        top: Float
    ) {
        val text = String.format(
            Locale.US, "%.1f%% · %s",
            point.probability * 100, dateFormat.format(Date(point.timestampMs))
        )
        val padH = 8 * density
        val padV = 5 * density
        val textWidth = tooltipTextPaint.measureText(text)
        val boxWidth = textWidth + 2 * padH
        val boxHeight = tooltipTextPaint.textSize + 2 * padV
        val boxLeft = (anchorX - boxWidth / 2).coerceIn(left, right - boxWidth)
        val boxTop = top
        val rect = RectF(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight)
        canvas.drawRoundRect(rect, 8 * density, 8 * density, tooltipBgPaint)
        canvas.drawText(
            text,
            boxLeft + padH,
            boxTop + padV + tooltipTextPaint.textSize * 0.85f,
            tooltipTextPaint
        )
    }

    private fun yFor(probability: Double, top: Float, bottom: Float): Float =
        bottom - ((bottom - top) * probability).toFloat()
}
