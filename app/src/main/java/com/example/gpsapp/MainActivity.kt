package com.example.gpsapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.gpsapp.ui.navigation.AppNavHost
import com.example.gpsapp.ui.theme.ThintureTechnologiesPvtLtdTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("🧭 MainActivity started")

        enableEdgeToEdge()
        setContent {
            ThintureTechnologiesPvtLtdTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Timber.i("🎯 Navigation controller initialized")
                    AppNavHost(navController = navController)
                }
            }
        }
    }
}