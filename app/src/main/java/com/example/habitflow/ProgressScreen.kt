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


@Composable
fun ProgressScreen(navController: NavController, habit: String) {
    // Dummy data for cigarettes smoked each day (replace with actual data logic)
    val weeklyCigarettes = listOf(Entry(1f, 15f), Entry(2f, 17f), Entry(3f, 14f), Entry(4f, 10f), Entry(5f, 7f), Entry(6f, 11f), Entry(7f, 5f), Entry(8f, 6f), Entry(9f, 4f), Entry(10f, 2f))
    val comparisonData = listOf(Entry(1f, 15f), Entry(2f, 13f), Entry(3f, 11f), Entry(4f, 9f), Entry(5f, 7f), Entry(6f, 5f), Entry(7f, 3f), Entry(8f, 2f), Entry(9f, 1f), Entry(10f, 0f))

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
                    text = "$habit",
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
                    LineChartView(dataSets = listOf(weeklyCigarettes, comparisonData))
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
                    LineChartView(dataSets = listOf(weeklyCigarettes, comparisonData))
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
                    LineChartView(dataSets = listOf(weeklyCigarettes, comparisonData))
                }
            }
        }
    }
}

@Composable
fun LineChartView(dataSets: List<List<Entry>>) {
    val lineDataSets = dataSets.mapIndexed { index, data ->
        val label = when (index) {
            0 -> "Tracked"  // First dataset label
            1 -> "Goal"     // Second dataset label
            else -> "Comparison" // Any additional datasets
        }
        LineDataSet(data, label).apply {
            // Customize each dataset (e.g., colors, values, etc.)
            color = when (index) {
                0 -> Color.parseColor("#006400")
                1 -> Color.GREEN
                else -> Color.BLUE // You can adjust the colors as needed
            }
            valueTextColor = Color.BLACK
            setDrawValues(false)
        }
    }

    val lineData = LineData(lineDataSets)

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