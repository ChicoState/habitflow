package com.example.habitflow

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet



@Composable
fun ProgressScreen(navController: NavController, habit: String) {
    // Dummy data for cigarettes smoked each day (replace with actual data logic)
    val parts = habit.split(":")
    val userData = if (parts[2] == "good" )
    { listOf(DataLists.goodWeeklyData, DataLists.goodMonthlyData, DataLists.goodOverallData) }
    else { listOf(DataLists.badWeeklyData, DataLists.badMonthlyData, DataLists.badOverallData) }
    val comparisonData = if (parts[2] == "good" )
    { listOf(DataLists.goodComparisonData1, DataLists.goodComparisonData2, DataLists.goodComparisonData3) }
    else { listOf(DataLists.badComparisonData1, DataLists.badComparisonData2, DataLists.badComparisonData3) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            ) {
                Button(
                    onClick = { navController.navigate("home") },
                    modifier = Modifier.align(Alignment.CenterStart).height(36.dp) // Smaller button
                ) {
                    Text("Back")
                }

                Text(
                    text = "Your Progress",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${parts[0]}",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        item {
            // Weekly progress section aligned to the left with line chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Weekly",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    LineChartView(dataSets = listOf(userData[0], comparisonData[0]), habit)
                }
            }
        }
            // Spacer to add some space between the graphs
        item { Spacer(modifier = Modifier.height(16.dp)) }

            // Monthly progress section aligned to the left with line chart
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Monthly",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    LineChartView(dataSets = listOf(userData[1], comparisonData[1]), habit)
                }
            }
        }
        // Spacer to add some space between the graphs
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Overall progress section aligned to the left with line chart
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Overall",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    LineChartView(dataSets = listOf(userData[2], comparisonData[2]), habit)
                }
            }
        }
    }
}

@Composable
fun LineChartView(dataSets: List<List<Entry>>, habit: String) {
    val parts = habit.split(":")
    val lineDataSets = mutableListOf<LineDataSet>()
    dataSets.mapIndexed { index, data ->
        val label = when (index) {
            0 -> "Tracked"  // First dataset label
            1 -> "Goal"     // Second dataset label
            else -> "Comparison" // Any additional datasets
        }

        val entriesWithRedDot = mutableListOf<Entry>()
        val newData = mutableListOf<Entry>()

        for (i in 1 until data.size - 1) {
            val currentEntry = data[i]
            val previousEntry = data[i - 1]
            val nextEntry = data[i + 1]

            if (currentEntry.y == 0f) {
                // Insert a red dot between previous and next entry
                val averageY = (previousEntry.y + nextEntry.y) / 2
                entriesWithRedDot.add(Entry(currentEntry.x, averageY))
                newData.add(Entry(currentEntry.x, averageY))
            } else {
                // Add regular point
                newData.add(currentEntry)
            }
        }
        lineDataSets.add(
            LineDataSet(newData, label).apply {
                // Customize each dataset (e.g., colors, values, etc.)
                when (index) {
                    0 -> {
                        color = Color.parseColor("#006400")
                    }
                    1 -> {
                        color = Color.argb(50, 0, 255, 0)
                        lineWidth = 5f
                    }
                    else -> {
                        color = Color.BLUE
                    }
                }
                valueTextColor = Color.BLACK
                setDrawValues(false)
                setDrawCircles(false) // Remove dots on the line
                setDrawFilled(false)
            }
        )
        lineDataSets.add(
            LineDataSet(entriesWithRedDot, "$label Red Dots").apply {
                setDrawCircles(true) // Show points as circles
                setCircleColor(Color.RED) // Red color for the circles
                setCircleRadius(3f) // Set circle radius
                color = Color.argb(0, 0, 0, 0)
                setDrawValues(false) // Don't draw values on the red dots
            }
        )

    }

    val lineData = LineData(lineDataSets as MutableList<ILineDataSet>)

    AndroidView(
        factory = {
            LineChart(it).apply {
                this.data = lineData
                this.invalidate() // Refresh the chart with new data
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)  // Adjust the height for the graph
    )
}