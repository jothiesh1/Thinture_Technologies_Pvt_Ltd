package com.example.gpsapp.ui.screens.client

import android.graphics.Color as AndroidColor
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.gpsapp.R
import com.example.gpsapp.data.model.DashboardViewModel
import com.example.gpsapp.ui.components.ScaffoldWithDrawer
import com.example.gpsapp.ui.screens.client.DashboardHeaderClient
import com.example.gpsapp.ui.screens.client.EnhancedDashboardDialog
import com.example.gpsapp.ui.screens.client.EnhancedDashboardSection
import com.example.gpsapp.ui.screens.client.StatsSummaryCardsClient
import com.example.gpsapp.ui.screens.client.configureAxes
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

@Composable
fun ClientDashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel? = null
) {
    val actualViewModel = viewModel ?: androidx.lifecycle.viewmodel.compose.viewModel()
    var dialogState by remember { mutableStateOf(false) }
    var dialogLabel by remember { mutableStateOf("") }
    var dialogValue by remember { mutableStateOf(0f) }
    var dialogIcon by remember { mutableStateOf<ImageVector>(Icons.Default.Info) }
    var dialogColor by remember { mutableStateOf(Color.Blue) }
    val status by actualViewModel.statusData.collectAsState()

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        actualViewModel.fetchVehicleStatus()
    }

    ScaffoldWithDrawer(
        navController = navController,
        screenTitle = "Dashboard",
        role = "client"
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.imagelogin),
                contentDescription = "Background image for dashboard",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically()
                ) {
                    DashboardHeaderClient()
                }

                Spacer(modifier = Modifier.height(24.dp))

                val totalVehicles = (status?.moving ?: 0) + (status?.idle ?: 0) +
                        (status?.parked ?: 0) + (status?.offline ?: 0)
                val activeVehicles = (status?.moving ?: 0) + (status?.idle ?: 0)

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                            slideInVertically(animationSpec = tween(600, delayMillis = 200))
                ) {
                    StatsSummaryCardsClient(
                        totalVehicles = totalVehicles,
                        activeVehicles = activeVehicles
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 400)) +
                            slideInVertically(animationSpec = tween(600, delayMillis = 400))
                ) {
                    EnhancedDashboardSection(
                        title = "Vehicle Status",
                        icon = Icons.Default.DirectionsCar,
                        labels = listOf("Running", "Idle", "Parked", "Offline"),
                        values = listOf(
                            (status?.moving ?: 0).toFloat(),
                            (status?.idle ?: 0).toFloat(),
                            (status?.parked ?: 0).toFloat(),
                            (status?.offline ?: 0).toFloat()
                        ),
                        barColors = listOf("#0066CC", "#FF9900", "#666666", "#CC0000"),
                        onBarTapped = { label, value ->
                            dialogLabel = label
                            dialogValue = value
                            dialogIcon = when(label) {
                                "Running" -> Icons.Default.PlayArrow
                                "Idle" -> Icons.Default.Pause
                                "Parked" -> Icons.Default.LocalParking
                                else -> Icons.Default.SignalCellularOff
                            }
                            dialogColor = when(label) {
                                "Running" -> Color(0xFF0066CC)
                                "Idle" -> Color(0xFFFF9900)
                                "Parked" -> Color(0xFF666666)
                                else -> Color(0xFFCC0000)
                            }
                            dialogState = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 600)) +
                            slideInVertically(animationSpec = tween(600, delayMillis = 600))
                ) {
                    EnhancedDashboardSection(
                        title = "User Distribution",
                        icon = Icons.Default.People,
                        labels = listOf("Dealers", "Clients", "Users"),
                        values = listOf(12f, 17f, 40f),
                        barColors = listOf("#0066CC", "#FF9900", "#009999"),
                        onBarTapped = { label, value ->
                            dialogLabel = label
                            dialogValue = value
                            dialogIcon = Icons.Default.Person
                            dialogColor = when(label) {
                                "Dealers" -> Color(0xFF0066CC)
                                "Clients" -> Color(0xFFFF9900)
                                else -> Color(0xFF009999)
                            }
                            dialogState = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (dialogState) {
                EnhancedDashboardDialog(
                    label = dialogLabel,
                    value = dialogValue,
                    icon = dialogIcon,
                    color = dialogColor,
                    onDismiss = { dialogState = false }
                )
            }
        }
    }
}
@Composable
private fun DashboardHeaderClient() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Dashboard,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Fleet Dashboard",
            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics {
                contentDescription = "Fleet Dashboard heading"
            }
        )
        Text(
            text = "Real-time Overview",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
private fun StatsSummaryCardsClient(
    totalVehicles: Int,
    activeVehicles: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Total Vehicles",
            value = totalVehicles.toString(),
            icon = Icons.Default.DirectionsCar,
            color = Color(0xFF0066CC),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Active Now",
            value = activeVehicles.toString(),
            icon = Icons.Default.Speed,
            color = Color(0xFF00AA44),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
private fun EnhancedDashboardSection(
    title: String,
    icon: ImageVector,
    labels: List<String>,
    values: List<Float>,
    barColors: List<String>,
    onBarTapped: (String, Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.semantics {
                        contentDescription = "$title section"
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            MinimalHorizontalBarChart(
                labels = labels,
                values = values,
                barColors = barColors,
                onBarTapped = onBarTapped
            )
        }
    }
}

@Composable
private fun EnhancedDashboardDialog(
    label: String,
    value: Float,
    icon: ImageVector,
    color: Color,
    onDismiss: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showDialog = true
    }

    AnimatedVisibility(
        visible = showDialog,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f)
    ) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onDismiss()
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(color.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Count",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value.toInt().toString(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    )
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        showDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = color.copy(alpha = 0.2f),
                        contentColor = color
                    )
                ) {
                    Text("Got it")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun MinimalHorizontalBarChart(
    labels: List<String>,
    values: List<Float>,
    barColors: List<String>,
    onBarTapped: (String, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val normalizedColors = remember(barColors, values) {
        if (barColors.size < values.size) {
            barColors + List(values.size - barColors.size) { "#9E9E9E" }
        } else {
            barColors
        }
    }

    val chart = remember(labels.size) {
        HorizontalBarChart(context).apply {
            configureChartAppearance()
            configureAxes(labels)
        }
    }

    LaunchedEffect(labels) {
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    val index = it.x.toInt()
                    if (index in labels.indices && index in values.indices) {
                        onBarTapped(labels[index], it.y)
                    }
                }
            }

            override fun onNothingSelected() {}
        })
    }

    LaunchedEffect(values, normalizedColors) {
        chart.clear()

        if (values.isNotEmpty()) {
            val entries = values.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
            val dataSet = BarDataSet(entries, "").apply {
                colors = normalizedColors.map {
                    try {
                        AndroidColor.parseColor(it)
                    } catch (e: IllegalArgumentException) {
                        AndroidColor.GRAY
                    }
                }
                valueTextSize = 13f
                valueTextColor = AndroidColor.WHITE
                setDrawValues(true)
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()
                    }
                }
            }

            chart.data = BarData(dataSet).apply {
                barWidth = 0.6f
            }
        }

        chart.animateY(800)
        chart.invalidate()
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .semantics {
                contentDescription = "Horizontal bar chart showing ${labels.joinToString()}"
            },
        factory = { chart }
    )
}

private fun HorizontalBarChart.configureChartAppearance() {
    setBackgroundColor(AndroidColor.TRANSPARENT)
    setDrawGridBackground(false)
    setDrawBorders(false)
    description.isEnabled = false
    legend.isEnabled = false
    setTouchEnabled(true)
    setScaleEnabled(false)
    setPinchZoom(false)
    isDoubleTapToZoomEnabled = false
    extraBottomOffset = 10f
}

private fun HorizontalBarChart.configureAxes(labels: List<String>) {
    axisLeft.isEnabled = false
    axisRight.apply {
        isEnabled = true
        axisMinimum = 0f
        textColor = AndroidColor.WHITE
        textSize = 11f
        setDrawGridLines(true)
        gridColor = AndroidColor.argb(40, 255, 255, 255)
        gridLineWidth = 1f
    }

    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        valueFormatter = IndexAxisValueFormatter(labels)
        setDrawGridLines(false)
        setDrawAxisLine(false)
        setDrawLabels(true)
        granularity = 1f
        isGranularityEnabled = true
        labelCount = labels.size
        textColor = AndroidColor.WHITE
        textSize = 11f
        yOffset = 5f
    }
}
