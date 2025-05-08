package com.example.habitflow.viewmodel

import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry

class LineChartViewModel : ViewModel() {

    data class ChartData(
        val mainData: List<Entry>,
        val redDotData: List<Entry>
    )

    fun generateChartData(entries: List<Entry>): ChartData {
        if (entries.size < 2) return ChartData(entries, emptyList())

        val redDotEntries = mutableListOf<Entry>()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        for (i in 1 until entries.size) {
            val prev = entries[i - 1]
            val curr = entries[i]

            val x1 = prev.x
            val x2 = curr.x
            val y1 = prev.y
            val y2 = curr.y

            val numMissingDays = ((x2 - x1) / oneDayMillis).toInt() - 1
            for (j in 1..numMissingDays) {
                val gapX = x1 + j * oneDayMillis
                val interpY = y1 + (gapX - x1) * (y2 - y1) / (x2 - x1)
                redDotEntries.add(Entry(gapX, interpY))
            }
        }

        return ChartData(mainData = entries, redDotData = redDotEntries)
    }
}
