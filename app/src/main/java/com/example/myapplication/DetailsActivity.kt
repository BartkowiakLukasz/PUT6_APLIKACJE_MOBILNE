package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ui.theme.MyApplicationTheme

class DetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val route = intent.getSerializableExtra("ROUTE_DATA") as? Route

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                var currentSeconds by rememberSaveable { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize().statusBarsPadding(),
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            val minutes = currentSeconds / 60
                            val seconds = currentSeconds % 60
                            val timeText = "Mój czas na trasie ${route?.name}: %02d:%02d".format(minutes, seconds)
                            Toast.makeText(context, timeText, Toast.LENGTH_LONG).show()
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Wyślij")
                        }
                    }
                ) { innerPadding ->
                    RouteDetailContent(
                        route = route,
                        modifier = Modifier.padding(innerPadding),
                        seconds = currentSeconds,
                        onSecondsChange = { currentSeconds = it }
                    )
                }
            }
        }
    }
}