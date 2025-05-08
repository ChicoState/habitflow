package com.example.habitflow.model

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import com.example.habitflow.ui.theme.hf_teal
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class AlternatingBackgroundRenderer(
    chart: LineChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineChartRenderer(chart, animator, viewPortHandler) {

    private val bandPaint1 = Paint().apply {
        style = Paint.Style.FILL
        color = hf_teal.copy(alpha = 0.05f).toArgb()
    }

    private val bandPaint2 = Paint().apply {
        style = Paint.Style.FILL
        color = hf_teal.copy(alpha = 0.15f).toArgb()
    }

    override fun drawExtras(c: Canvas) {
        super.drawExtras(c)
        drawAlternatingBands(c)
    }

    private fun drawAlternatingBands(canvas: Canvas) {
        val xAxis = (mChart as LineChart).xAxis
        val entries = xAxis.mEntries

        val trans = mChart.getTransformer(YAxis.AxisDependency.LEFT)
        val contentRect = mViewPortHandler.contentRect

        for (i in 0 until entries.size - 1) {
            val x1 = trans.getPixelForValues(entries[i], 0f).x
            val x2 = trans.getPixelForValues(entries[i + 1], 0f).x

            val paint = if (i % 2 == 0) bandPaint1 else bandPaint2

            canvas.drawRect(x1.toFloat(), contentRect.top, x2.toFloat(), contentRect.bottom, paint)
        }
    }
}
