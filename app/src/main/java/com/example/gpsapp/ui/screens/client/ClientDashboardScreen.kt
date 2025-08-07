package com.example.gpsapp.ui.screens.client

import android.graphics.Color.TRANSPARENT
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.gpsapp.R
import com.example.gpsapp.data.model.DashboardViewModel
import com.example.gpsapp.ui.components.ScaffoldWithDrawer
import com.example.gpsapp.ui.screens.dealer.MinimalHorizontalBarChart
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
    viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var dialogState by remember { mutableStateOf(false) }
    var dialogLabel by remember { mutableStateOf("") }
    var dialogValue by remember { mutableStateOf(0f) }
    val status by viewModel.statusData.collectAsState()


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
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Fleet Dashboard",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Vehicle Status",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))
                val running = (status?.moving ?: 0).toFloat()
                val idle = (status?.idle ?: 0).toFloat()
                val parked = (status?.parked ?: 0).toFloat()
                val offline = (status?.offline ?: 0).toFloat()

                MinimalHorizontalBarChart(
                    labels = listOf("Running", "Idle", "Parked", "Offline"),
                    values = listOf(running, idle, parked, offline),
                    barColors = listOf("#4CAF50", "#FFC107", "#2196F3", "#F44336"),
                    onBarTapped = { label, value ->
                        dialogLabel = label
                        dialogValue = value
                        dialogState = true
                    }
                )

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "User Distribution",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val users = 40f

                MinimalHorizontalBarChart(
                    labels = listOf("Users"),
                    values = listOf(users),
                    barColors = listOf("#03A9F4", "#8BC34A"),
                    onBarTapped = { label, value ->
                        dialogLabel = label
                        dialogValue = value
                        dialogState = true
                    }
                )
            }

            if (dialogState) {
                AlertDialog(
                    onDismissRequest = { dialogState = false },
                    title = { Text("Details for $dialogLabel") },
                    text = { Text("Value: $dialogValue") },
                    confirmButton = {
                        TextButton(onClick = { dialogState = false }) {
                            Text("OK")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                )
            }
        }
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

    val chart = remember {
        HorizontalBarChart(context).apply {
            setBackgroundColor(TRANSPARENT)
            setDrawGridBackground(false)
            setDrawBorders(false)
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setScaleEnabled(false)

            axisLeft.isEnabled = false
            axisRight.apply {
                isEnabled = true
                axisMinimum = 0f
                textColor = android.graphics.Color.LTGRAY
                textSize = 10f
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
                textColor = android.graphics.Color.LTGRAY
                textSize = 10f
            }

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        val index = it.x.toInt()
                        onBarTapped(labels[index], it.y)
                    }
                }

                override fun onNothingSelected() {}
            })
        }
    }

    LaunchedEffect(values) {
        val entries = values.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val dataSet = BarDataSet(entries, "").apply {
            colors = barColors.map { android.graphics.Color.parseColor(it) }
            valueTextSize = 12f
            valueTextColor = android.graphics.Color.WHITE
            setDrawValues(true)
        }

        chart.data = BarData(dataSet).apply {
            barWidth = 0.5f
        }

        chart.invalidate() // Refresh chart display
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(260.dp),
        factory = { chart }
    )
}


@Preview(showBackground = true)
@Composable
fun PreviewDashboardScreen() {
    ClientDashboardScreen(navController = rememberNavController())
}
