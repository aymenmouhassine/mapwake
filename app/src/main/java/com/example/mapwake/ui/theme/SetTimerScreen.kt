package com.example.mapwake.ui

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mapwake.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.util.Locale
import androidx.compose.ui.graphics.Color
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetTimerScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Alarm") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        TimerContent(Modifier.padding(paddingValues))
    }
}

@Composable
fun TimerContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val initialHours = 0
    val initialMinutes = 5
    val initialSeconds = 0
    var hours by remember { mutableIntStateOf(initialHours) }
    var minutes by remember { mutableIntStateOf(initialMinutes) }
    var seconds by remember { mutableIntStateOf(initialSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(1f) }
    var timerJob: Job? by remember { mutableStateOf(null) }

    val coroutineScope = rememberCoroutineScope()
    val totalSeconds = (hours * 3600 + minutes * 60 + seconds).toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Set alarm",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(120.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 8.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        TimeSelector(label = "Hours", value = hours, onIncrease = { if (hours < 23) hours++ }, onDecrease = { if (hours > 0) hours-- })
        TimeSelector(label = "Minutes", value = minutes, onIncrease = { if (minutes < 59) minutes++ }, onDecrease = { if (minutes > 0) minutes-- })
        TimeSelector(label = "Seconds", value = seconds, onIncrease = { if (seconds < 59) seconds++ }, onDecrease = { if (seconds > 0) seconds-- })

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isRunning) {
                    isRunning = false
                    timerJob?.cancel()
                } else {
                    isRunning = true
                    timerJob = coroutineScope.launch {
                        startTimer(context, hours, minutes, seconds) { h, m, s ->
                            hours = h
                            minutes = m
                            seconds = s
                            progress = ((h * 3600 + m * 60 + s) / totalSeconds).coerceAtLeast(0f)
                        }
                        isRunning = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = if (isRunning) "Stop" else "Start")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                isRunning = false
                timerJob?.cancel()
                hours = initialHours
                minutes = initialMinutes
                seconds = initialSeconds
                progress = 1f
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Reset")
        }
    }
}

@Composable
fun TimeSelector(label: String, value: Int, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onDecrease) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease $label")
        }
        Text(
            text = String.format(Locale.getDefault(), "%02d", value),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onIncrease) {
            Icon(Icons.Filled.Add, contentDescription = "Increase $label")
        }
    }
}

suspend fun startTimer(
    context: Context,
    initialHours: Int,
    initialMinutes: Int,
    initialSeconds: Int,
    onTick: (Int, Int, Int) -> Unit
) {
    var hours = initialHours
    var minutes = initialMinutes
    var seconds = initialSeconds

    while (hours > 0 || minutes > 0 || seconds > 0) {
        delay(1000)

        if (seconds > 0) {
            seconds--
        } else {
            seconds = 59
            if (minutes > 0) {
                minutes--
            } else {
                minutes = 59
                if (hours > 0) {
                    hours--
                }
            }
        }

        onTick(hours, minutes, seconds)
    }

    onTick(0, 0, 0)
    showNotification(context)
    playAlertSound(context)
}

@SuppressLint("MissingPermission")
fun showNotification(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
    }

    val notification = NotificationCompat.Builder(context, "timer_channel")
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("Timer Finished")
        .setContentText("The timer has completed.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    NotificationManagerCompat.from(context).notify(1, notification)
}

fun playAlertSound(context: Context) {
    try {
        // Libérer toute instance de MediaPlayer existante avant d'en créer une nouvelle
        var mediaPlayer = MediaPlayer.create(context, R.raw.alert_sound)
        mediaPlayer?.setOnPreparedListener {
            Log.d("SetTimerScreen", "MediaPlayer is prepared and ready to play.")
            mediaPlayer.start()
        }

        // Gérer l'événement de fin de lecture pour libérer le MediaPlayer
        mediaPlayer?.setOnCompletionListener {
            Log.d("SetTimerScreen", "MediaPlayer completed playing alert sound.")
            it.release()
        }

        // Ajouter un gestionnaire d'erreurs
        mediaPlayer?.setOnErrorListener { mp, what, extra ->
            Log.e("SetTimerScreen", "Error occurred in MediaPlayer: what=$what, extra=$extra")
            mp.release()
            true // On indique que l'erreur a été gérée
        }

        Log.d("SetTimerScreen", "Attempting to play alert sound.")

    } catch (e: Exception) {
        Log.e("SetTimerScreen", "Exception while playing alert sound", e)
    }
}

