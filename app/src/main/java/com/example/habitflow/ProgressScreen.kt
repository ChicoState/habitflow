package com.example.habitflow

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.components.XAxis
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import android.graphics.Color as AndroidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitflow.model.AlternatingBackgroundRenderer
import com.example.habitflow.model.UserData
import com.example.habitflow.ui.theme.hf_teal
import com.example.habitflow.viewmodel.AddDataViewModel
import com.example.habitflow.viewmodel.LineChartViewModel
import com.example.habitflow.viewmodel.ProgressViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun ProgressScreen(
    dataViewModel: AddDataViewModel,
    navController: NavController,
    habitId: String,
    userDataId: String,
    span: String,
    sharedPreferences: SharedPreferences
) {

    val viewModel: ProgressViewModel = viewModel()
    val userData by viewModel.spanData.collectAsState()
    val chartViewModel: LineChartViewModel = viewModel()
    val chartData = chartViewModel.generateChartData(userData)
    val habitState by viewModel.habit.collectAsState()
    val dataState by viewModel.userData.collectAsState()
    val habitName = viewModel.getHabitName()
    val progress = viewModel.getUserProgress()
    val streak = viewModel.getStreak()
    val deadline = viewModel.getDeadline()
    val todayValue = viewModel.getTodayValue()
    val buttons = viewModel.getSpanButtons(habitId, userDataId)
    val darkMode by remember { mutableStateOf(sharedPreferences.getBoolean("dark_mode", false)) }
    val navBackStackEntry = remember(habitId, userDataId, span) { navController.getBackStackEntry("progress/${habitId}/${userDataId}/$span") }
    val result = navBackStackEntry.savedStateHandle.getLiveData<Boolean>("dataUpdated")
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(span) {
        viewModel.setSpan(span)
    }
    LaunchedEffect(habitId) {
        viewModel.loadHabit(habitId)
        viewModel.loadUserData(userDataId)
    }
    LaunchedEffect(result, lifecycleOwner) {
        result.observe(lifecycleOwner) { updated ->
            if (updated == true) {
                viewModel.loadUserData(userDataId)
                navBackStackEntry.savedStateHandle["dataUpdated"] = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProgressHeader(
                habitName = habitName,
                darkMode = darkMode,
                onBackClick = { navController.navigate("home/") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                dataState?.let { userData ->
                    if (viewModel.shouldPromptEntry()) {
                        DataEntryPromptCard(
                            onAddClick = {
                                habitState?.let { habit ->
                                    dataViewModel.setHabit(habit)
                                    dataViewModel.setUserData(userData)
                                    navController.navigate("addData")
                                }
                            }
                        )
                    } else {
                        if (todayValue != null) {
                            TodayEntryCard(
                                todayValue = todayValue,
                                units = habitState?.units ?: "",
                                onClick = {
                                    habitState?.let { habit ->
                                        dataViewModel.setHabit(habit)
                                        dataViewModel.setUserData(userData)
                                        navController.navigate("addData")
                                    }
                                }
                            )
                        }
                    }
                }
            }
            if (userData.isNotEmpty()) {
                if (userData.size > 1) {
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = "$span Progress",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            LineChartView(chartData = chartData)
                        }
                    }
                }
                if (dataState?.userData?.size!! > 1) {
                    Spacer(modifier = Modifier.padding(8.dp))
                    SpanSelectorButtons(buttons = buttons) { route ->
                        navController.navigate(route)
                    }
                    Spacer(modifier = Modifier.padding(8.dp))
                    EncouragementRow(dataState = dataState!!, message = viewModel.getEncouragementMessage())
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(top = 30.dp, bottom = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Trackable data for this habit yet.",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                StreakBadge(streak = streak)
                Spacer(modifier = Modifier.padding(8.dp))
                if (deadline != null && progress != null) {
                    ProgressRing(progress = progress, totalDays = userData.size)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No data for this habit yet.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressHeader(
    habitName: String,
    darkMode: Boolean,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .padding(5.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = if (darkMode) Color.White else Color.Black
            )
        }

        Text(
            text = habitName,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun DataEntryPromptCard(
    onAddClick: () -> Unit
) {
    Surface(
        color = Color(0xFFFFCDD2).copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp, vertical = 14.dp)
                .fillMaxWidth()
                .wrapContentHeight(Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "You haven't entered data for today yet!",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(onClick = onAddClick, shape = RoundedCornerShape(6.dp)) {
                Text("Add Data")
            }
        }
    }
}

@Composable
fun TodayEntryCard(todayValue: Float, units: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.92f),
                color = hf_teal.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 28.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Today's entry: $todayValue $units",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Click to update",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StreakBadge(streak: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.background(Color.Transparent)) {
            Text(
                text = "🔥",
                style = TextStyle(
                    fontSize = 60.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
            Text(
                text = "$streak",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.padding(5.dp))
        Text(
            text = "You have reached $streak consecutive days of tracking your goal! ",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
        )
    }
}

@Composable
fun LineChartView(chartData: LineChartViewModel.ChartData) {
    class DateAxisFormatter : ValueFormatter() {
        private val dateFormat = SimpleDateFormat("MM/dd", Locale.US)
        override fun getFormattedValue(value: Float): String {
            return dateFormat.format(Date(value.toLong()))
        }
    }

    if (chartData.mainData.isEmpty()) return

    val mainDataSet = LineDataSet(chartData.mainData, "Your Data").apply {
        color = hf_teal.toArgb()
        valueTextColor = AndroidColor.BLACK
        setDrawValues(false)
        setDrawCircles(true)
        circleRadius = 4f
        setDrawCircleHole(false)
        setCircleColor(hf_teal.toArgb())
        setDrawFilled(false)
        lineWidth = 2f
    }

    val redDotDataSet = LineDataSet(chartData.redDotData, "Missing Days").apply {
        setDrawCircles(true)
        setDrawValues(false)
        color = AndroidColor.TRANSPARENT
        setCircleColor(ColorUtils.setAlphaComponent(AndroidColor.RED, 100))
        circleRadius = 3f
        setDrawCircleHole(true)
        circleHoleColor = AndroidColor.WHITE
        circleHoleRadius = 2f
    }

    val lineData = LineData(listOf(mainDataSet, redDotDataSet))

    key(chartData) {
        AndroidView(
            factory = { context ->
                LineChart(context).apply {
                    this.data = lineData
                    animateXY(1500, 1500, Easing.EaseInOutQuad)
                    description.isEnabled = false
                    legend.isEnabled = false
                    xAxis.axisLineWidth = 2f
                    axisLeft.axisLineWidth = 2f

                    setDrawGridBackground(false)

                    renderer = AlternatingBackgroundRenderer(this, animator, viewPortHandler)

                    axisLeft.apply {
                        isEnabled = true
                        setDrawGridLines(true)
                        axisMaximum = mainDataSet.yMax + 1f
                        axisLineColor = hf_teal.toArgb()
                        textColor = AndroidColor.parseColor("#B0B0B0")

                    }
                        .gridColor = hf_teal.copy(alpha = 0.2f).toArgb()
                    axisRight.isEnabled = false

                    xAxis.apply {
                        isEnabled = true
                        setDrawGridLines(false)
                        position = XAxis.XAxisPosition.BOTTOM
                        valueFormatter = DateAxisFormatter()
                        granularity = 1f
                        axisLineColor = hf_teal.toArgb()
                        textColor = AndroidColor.parseColor("#B0B0B0")
                    }

                    invalidate()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

@Composable
fun ProgressRing(progress: Float, totalDays: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = size.minDimension
                val radius = canvasSize / 2f
                val sweepAngle = 360f * (progress / 100f)

                drawCircle(
                    color = Color.Gray.copy(alpha = 0.3f),
                    radius = radius
                )

                drawArc(
                    color = hf_teal.copy(alpha = 0.7f),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(canvasSize, canvasSize)
                )
            }
            Text(
                text = "${progress.toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.padding(5.dp))
        Text(
            text = "You have successfully tracked this habit for a total of $totalDays days!",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
        )
    }
}

@Composable
fun SpanSelectorButtons(
    buttons: List<Pair<String, String>>,
    onNavigate: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        buttons.forEach { (label, route) ->
            Button(
                onClick = { onNavigate(route) },
                modifier = Modifier.width(85.dp).height(36.dp),
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(label)
            }
        }
    }
}

@Composable
fun EncouragementRow(dataState: UserData, message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrendDisplay(dataState = dataState)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
        )
    }
}

@Composable
fun TrendDisplay(dataState: UserData, modifier: Modifier = Modifier) {
    val trendLine = dataState.trendDrawable
    Image(
        painter = painterResource(id = trendLine),
        contentDescription = "Trend Arrow",
        modifier = modifier
            .size(70.dp)
            .alpha(0.5f)
    )
}
