package com.example.mapwake

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mapwake.ui.ConfigurationScreen
import com.example.mapwake.ui.SetMusicScreen
import com.example.mapwake.ui.SetTimerScreen
import com.example.mapwake.ui.theme.MapwakeTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapwakeTheme {
                val navController = rememberNavController()
                MainScreen(navController)
            }
        }
        // Créer le canal de notification
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Timer Channel"
            val descriptionText = "Channel for timer notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("timer_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("configuration") { ConfigurationScreen(navController) }
        composable("set_music") { SetMusicScreen(navController) }
        composable("set_timer") { SetTimerScreen(navController) }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Icône de paramètres en haut à droite
        IconButton(
            onClick = { navController.navigate("configuration") },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings Icon")
        }

        // Trois boutons en bas, centrés horizontalement
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeButton(text = "Configuration") { navController.navigate("configuration") }
            HomeButton(text = "Set Music") { navController.navigate("set_music") }
            HomeButton(text = "Set Timer") { navController.navigate("set_timer") }
        }
    }
}

@Composable
fun HomeButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .width(200.dp)
            .height(48.dp)
    ) {
        Text(text = text, color = MaterialTheme.colorScheme.onPrimary)
    }
}
