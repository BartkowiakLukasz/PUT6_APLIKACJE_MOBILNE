package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun RouteStopwatch() {
    var seconds by rememberSaveable { mutableIntStateOf(0) }
    var isRunning by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (true) {
                delay(1000L)
                seconds++
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
            Text(
                text = "%02d:%02d".format(minutes, remainingSeconds),
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
                    seconds = 0
                }) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}