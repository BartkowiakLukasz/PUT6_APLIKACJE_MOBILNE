package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun RouteStopwatch(route: Route?, seconds: Int, onSecondsChange: (Int) -> Unit) {

    var isRunning by rememberSaveable { mutableStateOf(false) }

    var savedTimes by rememberSaveable { mutableStateOf(listOf<String>()) }

    val latestSeconds by rememberUpdatedState(newValue = seconds)

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (true) {
                delay(1000L)
                onSecondsChange(latestSeconds + 1)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Czas wyprawy", style = MaterialTheme.typography.labelLarge)

            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            val timeString = "%02d:%02d".format(minutes, remainingSeconds)

            Text(
                text = timeString,
                style = MaterialTheme.typography.displayMedium,
                fontSize = 48.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { isRunning = true }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(32.dp))
                }

                IconButton(onClick = { isRunning = false }) {
                    Icon(Icons.Default.Pause, contentDescription = "Przerwij", modifier = Modifier.size(32.dp))
                }

                IconButton(onClick = {
                    isRunning = false
                    onSecondsChange(0)
                }) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(32.dp))
                }

                IconButton(onClick = {
                    val formatter = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.getDefault())
                    val currentDateTime = formatter.format(java.util.Date())
                    val routeName = route?.name ?: "Nieznana trasa"
                    val record = "$currentDateTime | $routeName - $timeString"

                    savedTimes = savedTimes + record
                }) {
                    Icon(Icons.Default.Save, contentDescription = "Zapisz czas", modifier = Modifier.size(32.dp))
                }
            }

            if (savedTimes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Zapisane czasy:", style = MaterialTheme.typography.titleSmall)
                savedTimes.forEach { record ->
                    Text(text = record, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}