package com.example.habitflow

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.habitflow.repository.HabitRepository
import com.example.habitflow.viewmodel.StatsViewModel
import com.example.habitflow.viewmodel.StatsViewModelFactory
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    statsViewModel: StatsViewModel = viewModel(
        factory = StatsViewModelFactory(HabitRepository, FirebaseAuth.getInstance())
    )
) {    Column(modifier = Modifier.fillMaxSize()) {

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
            val habits by statsViewModel.habits.collectAsState()
            val isLoading by statsViewModel.isLoading.collectAsState()
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Total Habits: ${habits.size}")
                Text("Longest Streak: TBD days") // We'll replace "TBD" later
                Text("Habits Completed This Week: TBD") // We'll replace "TBD" later
            }

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


