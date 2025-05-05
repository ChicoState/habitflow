package com.example.habitflow

import android.util.Log
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*

import com.example.habitflow.viewmodel.StatsPageViewModel
import com.example.habitflow.viewmodel.StatsPageViewModelFactory
import com.github.mikephil.charting.components.XAxis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsPage(navController: NavController) {
    val viewModel: StatsPageViewModel = viewModel()
    val totalHabits by viewModel.totalHabits.collectAsState()
    val longestStreak by viewModel.longestStreak.collectAsState()
    val completionsThisWeek by viewModel.completionsThisWeek.collectAsState()
    val dailyCompletions by viewModel.dailyCompletions.collectAsState()
    val averageStreaks by viewModel.averageStreaks.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStats()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // This is all the Top Bar
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Habit Stats",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // Everything below the TopAppBar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text("Total Habits: $totalHabits")
            Text("Longest Streak: $longestStreak days")
            Text("Habits Completed This Week: $completionsThisWeek")

            Spacer(modifier = Modifier.height(30.dp))

            Log.d("StatsDebug", "Line chart data: $averageStreaks")
            Log.d("StatsDebug", "Bar chart data: $dailyCompletions")

            Text("Weekly Streak Progress", style = MaterialTheme.typography.titleLarge)
            LineChartView(data = averageStreaks)

            Spacer(modifier = Modifier.height(30.dp))

            Text("Habits Completed Per Day", style = MaterialTheme.typography.titleLarge)
            BarChartView(data = dailyCompletions)
        }
    }
}

@Composable
fun LineChartView(data: List<Float>) {
    val entries = data.mapIndexed { index, value -> Entry(index.toFloat(), value) }
    val lineData = LineData(LineDataSet(entries, "Streak").apply {
        color = AndroidColor.BLUE
        valueTextColor = AndroidColor.BLACK
        circleRadius = 4f
        setDrawValues(false)
        setDrawCircles(true)
    })

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            LineChart(context).apply {
                this.description = Description().apply { text = "" }
                this.axisLeft.axisMinimum = 0f
                this.axisRight.isEnabled = false
                this.xAxis.position = XAxis.XAxisPosition.BOTTOM
                this.legend.isEnabled = false
            }
        },
        update = { chart ->
            chart.data = lineData
            chart.invalidate()
        }
    )
}


@Composable
fun BarChartView(data: List<Float>) {
    val entries = data.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
    val barData = BarData(BarDataSet(entries, "Completions").apply {
        color = AndroidColor.MAGENTA
        valueTextColor = AndroidColor.BLACK
        setDrawValues(false)
    })

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            BarChart(context).apply {
                this.description = Description().apply { text = "" }
                this.axisLeft.axisMinimum = 0f
                this.axisRight.isEnabled = false
                this.xAxis.position = XAxis.XAxisPosition.BOTTOM
                this.legend.isEnabled = false
            }
        },
        update = { chart ->
            chart.data = barData
            chart.invalidate()
        }
    )
}

/*
@Composable
fun LineChartView(data: List<Float>) {
    val entries = data.mapIndexed { index, value -> Entry(index.toFloat(), value) }
    val dataSet = LineDataSet(entries, "Streak").apply {
        color = AndroidColor.BLUE
        valueTextColor = AndroidColor.BLACK
        circleRadius = 4f
    }

    val lineData = LineData(dataSet)

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            LineChart(context).apply {
                this.data = lineData
                this.description = Description().apply { text = "" }
                this.animateX(1000)
            }
        }
    )
}

@Composable
fun BarChartView(data: List<Float>) {
    val entries = data.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
    val dataSet = BarDataSet(entries, "Completions").apply {
        color = AndroidColor.MAGENTA
        valueTextColor = AndroidColor.BLACK
    }

    val barData = BarData(dataSet)

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            BarChart(context).apply {
                this.data = barData
                this.description = Description().apply { text = "" }
                this.animateY(1000)
            }
        }
    )
}
*/