package com.example.habitflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import android.graphics.Color as AndroidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
///// new import
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack


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


    ////// Adding in variables for calculating streak, progress, and overacheiver data, and dates


    val progress = ((userDataList[2][userDataList[2].size-1].x) / comparisonDataList[2].size * 100) //.toFloat()
    val streak =
        if (parts[2] == "good")
        { (countMatchingFromEndGood(userDataList[2], comparisonDataList[2])) }
        else { (countMatchingFromEndBad(userDataList[2], comparisonDataList[2])) }

    val larger =
        if (parts[2] == "good" )
        { countDaysWithLargerY(userDataList[2], comparisonDataList[2]) }
        else { countDaysWithSmallerY(userDataList[2], comparisonDataList[2]) }
    val goalMet = compareLists(userData, comparisonData)
    val dates = convertToDates(userData, "2/5/25")


    ////////////////////////////////////////////////////////////////////////////////////



    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp), /// changed padding to be less
            horizontalAlignment = Alignment.CenterHorizontally // Optionally, you can also center vertically if needed

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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Text(
                    text = parts[0],
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Center)

                )
            }
            Spacer(modifier = Modifier.height(16.dp))


            //////////////////// Adding in three top rows for prgoress, streak, and overacheiver


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), // Add padding as necessary
                horizontalArrangement = Arrangement.Start, // Align items to the left
                verticalAlignment = Alignment.CenterVertically // Align vertically in the center
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(Color.White, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasSize = size.minDimension
                        val radius = canvasSize / 2f
                        val sweepAngle = 360f * (progress / 100f)

                        // Draw the full circle
                        drawCircle(
                            color = Color.Gray.copy(alpha = 0.3f),
                            radius = radius
                        )

                        // Draw the filled portion of the pie chart
                        drawArc(
                            color = Color(0xFF00C853), // Green color for the filled portion
                            startAngle = -90f, // Starting from the top
                            sweepAngle = sweepAngle, // Percentage-based angle
                            useCenter = true,
                            size = Size(canvasSize, canvasSize)
                        )
                    }

                    // Display the percentage inside the pie chart
                    Text(
                        text = "${progress.toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    text = "You have completed ${userDataList[2].size} days of your ${comparisonDataList[2].size} day goal! ",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp) // Adjust font size as needed
                )
            }
            //Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), // Add padding as necessary
                horizontalArrangement = Arrangement.Start, // Align items to the left
                verticalAlignment = Alignment.CenterVertically // Align vertically in the center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp) // You can change the size according to your needs
                        .background(Color.Transparent), // Ensures background is transparent
                    //contentAlignment = Alignment.Center
                ) {
                    // Fire emoji as background
                    Text(
                        text = "🔥",
                        style = TextStyle(
                            fontSize = 45.sp, // Adjust the emoji size
                            color = Color.Gray.copy(alpha = 0.7f), // You can adjust the color of the emoji
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = Modifier
                            .align(Alignment.Center), // Center the text vertically over the emoji
                        textAlign = TextAlign.Center // Center the fire emoji
                    )

                    // Text on top of the fire emoji
                    Text(
                        text = "$streak",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Center), // Center the text vertically over the emoji
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    text = "You have reached $streak consecutive days of reaching your daily goal! ",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp) // Adjust font size as needed
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), // Add padding as necessary
                horizontalArrangement = Arrangement.Start, // Align items to the left
                verticalAlignment = Alignment.CenterVertically // Align vertically in the center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp) // You can change the size according to your needs
                        .background(Color.Transparent), // Ensures background is transparent
                    //contentAlignment = Alignment.Center
                ) {
                    // Fire emoji as background
                    Text(
                        text = "⭐",
                        style = TextStyle(
                            fontSize = 45.sp, // Adjust the emoji size
                            color = Color.Gray.copy(alpha = 0.6f), // You can adjust the color of the emoji
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = Modifier
                            .align(Alignment.Center), // Center the text vertically over the emoji
                        textAlign = TextAlign.Center // Center the fire emoji
                    )

                    // Text on top of the fire emoji
                    Text(
                        text = "$larger",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Center), // Center the text vertically over the emoji
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    text = "You have reached a total of $larger days of exceeding your daily goal! ",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp) // Adjust font size as needed
                )
            }


            //////////////////////////////////////////////////////////////////////////////////


            //////////////// Moving buttons to the middle of the screen: weekly, montly, and overall


            Spacer(modifier = Modifier.padding(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    //.align(Alignment.BottomCenter) // Align the Row at the bottom
                    //.background(androidx.compose.ui.graphics.Color.White)
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 16.dp), // Space at the bottom
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button 1
                Button(
                    onClick = { navController.navigate("progress/${habit}/Weekly") },
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
            Spacer(modifier = Modifier.padding(8.dp))


            ////////////////////////////////////////////////////////////////////////////////////


            Text(
                text = "$span Progress",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally) // This centers the text horizontally
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    LineChartView(dataSets = listOf(userData, comparisonData), habit)
                }
            }


            /////////////////////////////////////////////// Adding table with userData components


            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray) // Background color for the header
                        .padding(8.dp), // Padding around header text
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Day",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
                    )
                    Text(
                        text = "Your Data",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
                    )
                }

                // Table Data - LazyColumn with rows
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(goalMet.reversed()) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                //.padding(vertical = 1.dp) // Padding between rows
                                .background(if (goalMet.indexOf(entry) % 2 == 0) Color.LightGray else Color.Transparent) // Alternating row colors
                                .border(1.dp, Color.Gray), // Border around each row
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = entry.x.toString(),
                                modifier = Modifier
                                    .padding(start = 8.dp), // Padding around text
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = entry.y.toString(),
                                modifier = Modifier
                                    .padding(2.dp), // Padding around text
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }


            ///////////////////////////////////////////////////////////////////////////////////


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