package com.example.habitflow

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
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsPage(navController: NavController) {
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

            Text("Total Habits: 7")
            Text("Longest Streak: 12 days")
            Text("Habits Completed This Week: 24")

            Spacer(modifier = Modifier.height(30.dp))

            Text("Weekly Streak Progress", style = MaterialTheme.typography.titleLarge)
            LineChartView(data = listOf(1f, 3f, 5f, 4f, 6f, 7f, 8f))

            Spacer(modifier = Modifier.height(30.dp))

            Text("Habits Completed Per Day", style = MaterialTheme.typography.titleLarge)
            BarChartView(data = listOf(2f, 3f, 5f, 4f, 6f, 4f, 7f))
        }
    }
}

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


