package com.example.thinturetechnologiespvtltd.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.thinturetechnologiespvtltd.R
import com.example.thinturetechnologiespvtltd.ui.components.ScaffoldWithDrawer

@Composable
fun DashboardScreen(navController: NavController) {
    ScaffoldWithDrawer(navController = navController, screenTitle = "Dashboard") { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.imagelogin),
                contentDescription = "Background Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with title
                Text(
                    text = "Fleet Management Dashboard",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White, // Light text for contrast
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .shadow(4.dp) // Apply shadow to text
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewDashboardScreen() {
    // Use rememberNavController() for the preview
    DashboardScreen(navController = rememberNavController())
}
