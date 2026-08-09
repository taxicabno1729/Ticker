package com.example.liveticker.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.example.liveticker.R
import com.example.liveticker.data.ProbabilityPoint

/**
 * Minimal probability line chart: gridlines at 0/50/100%, polyline,
 * gradient fill under the line. Trend up (or flat) draws in accent_green,
 * trend down in accent_red. No touch interaction.
 */
class ProbabilityChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var points: List<ProbabilityPoint> = emptyList()

    private val density = resources.displayMetrics.density
    private val labelWidth = 32 * density
    private val chartPadding = 4 * density

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

    fun setPoints(points: List<ProbabilityPoint>) {
        this.points = points
        invalidate()
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

        val trendUp = points.last().probability >= points.first().probability
        val color = context.getColor(if (trendUp) R.color.accent_green else R.color.accent_red)
        linePaint.color = color
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
    }

    private fun yFor(probability: Double, top: Float, bottom: Float): Float =
        bottom - ((bottom - top) * probability).toFloat()
}
