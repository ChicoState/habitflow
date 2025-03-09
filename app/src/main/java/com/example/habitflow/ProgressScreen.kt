package com.example.habitflow

import androidx.compose.foundation.background
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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.components.XAxis
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import androidx.compose.ui.text.font.FontWeight


@Composable
fun ProgressScreen(navController: NavController, habit: String, span: String) {
    // Dummy data for cigarettes smoked each day (replace with actual data logic)
    val parts = habit.split(":")
    val userDataList = if (parts[2] == "good" )
    { listOf(DataLists.goodWeeklyData, DataLists.goodMonthlyData, DataLists.goodOverallData) }
    else { listOf(DataLists.badWeeklyData, DataLists.badMonthlyData, DataLists.badOverallData) }
    val comparisonDataList = if (parts[2] == "good" )
    { listOf(DataLists.goodComparisonData1, DataLists.goodComparisonData2, DataLists.goodComparisonData3) }
    else { listOf(DataLists.badComparisonData1, DataLists.badComparisonData2, DataLists.badComparisonData3) }
    val userData =
        if (span == "Weekly") { userDataList[0] }
        else if (span == "Monthly") { userDataList[1] }
        else { userDataList[2] }
    val comparisonData =
        if (span == "Weekly") { comparisonDataList[0] }
        else if (span == "Monthly") { comparisonDataList[1] }
        else { comparisonDataList[2] }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.Center // Optionally, you can also center vertically if needed

        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    //.background(androidx.compose.ui.graphics.Color.Gray)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                IconButton(
                    onClick = { navController.navigate("home/") }, // Navigate to "home/"
                    modifier = Modifier
                        .size(40.dp) // Increase the size of the icon button for the bubble effect
                        .background(
                            color = Color(0xFFE0E0E0), // Correct Color usage
                            shape = CircleShape // Make the background circular
                        )
                        .padding(5.dp)
                        .align(Alignment.TopStart)

                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Text(
                    text = "${parts[0]}",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Center)

                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your $span Progress",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally) // This centers the text horizontally
            )
            /*LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
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
                */
                // Monthly progress section aligned to the left with line chart

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    LineChartView(dataSets = listOf(userData, comparisonData), habit)
                }
            }
            /*
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
                }*/
            //}
            Spacer(modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                //.height(100.dp)
                .align(Alignment.BottomCenter) // Align the Row at the bottom
                .background(androidx.compose.ui.graphics.Color.White)
                .padding(horizontal = 10.dp)
                .padding(bottom = 16.dp), // Space at the bottom
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button 1
            Button(
                onClick = {navController.navigate("progress/${habit}/Weekly") },
                modifier = Modifier.width(85.dp).height(48.dp),
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(0.dp) // Removes the internal padding

            ) {
                Text(
                    text = "Weekly"
                )
            }

            // Button 2
            Button(
                onClick = { navController.navigate("progress/${habit}/Monthly") },
                modifier = Modifier.width(85.dp).height(48.dp),
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(0.dp) // Removes the internal padding

            ) {
                Text("Monthly")
            }

            // Button 3
            Button(
                onClick = { navController.navigate("progress/${habit}/Overall") },
                modifier = Modifier.width(85.dp).height(48.dp),
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(0.dp) // Removes the internal padding
            ) {
                Text("Overall")
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

            if ((currentEntry.x - previousEntry.x) > 1) {
                // Insert a red dot between previous and next entry
                val averageY = (previousEntry.y + nextEntry.y) / 2
                entriesWithRedDot.add(Entry(currentEntry.x-1, averageY))
                newData.add(Entry(currentEntry.x-1, averageY))
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
                        color = AndroidColor.parseColor("#006400")
                    }
                    1 -> {
                        color = AndroidColor.argb(128, 0, 255, 0)
                        lineWidth = 5f
                    }
                    else -> {
                        color = AndroidColor.BLUE
                    }
                }
                valueTextColor = AndroidColor.BLACK
                setDrawValues(false)
                setDrawCircles(false) // Remove dots on the line
                setDrawFilled(false)
            }
        )
        lineDataSets.add(
            LineDataSet(entriesWithRedDot, "Missed").apply {
                setDrawCircles(true) // Show points as circles
                setCircleColor(AndroidColor.RED) // Red color for the circles
                setCircleRadius(3f) // Set circle radius
                color = 0x00000000
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
                val legend = this.legend
                legend.isEnabled = false
                this.description.isEnabled = false // Disable the description
                this.axisLeft.isEnabled = true // Disable left Y axis
                this.axisRight.isEnabled = false // Enable right Y axis
                this.xAxis.isEnabled = true // Enable the bottom X axis
                this.xAxis.position = XAxis.XAxisPosition.BOTTOM // Ensure it's at the bottom
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)  // Adjust the height for the graph
    )
}