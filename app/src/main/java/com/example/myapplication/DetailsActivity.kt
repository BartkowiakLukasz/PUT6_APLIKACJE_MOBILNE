package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val route = intent.getSerializableExtra("ROUTE_DATA") as? Route
        val initialDarkMode = intent.getBooleanExtra("DARK_MODE", false)

        setContent {
            var isDarkMode by rememberSaveable { mutableStateOf(initialDarkMode) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val dao = remember { AppDatabase.getDatabase(context).routeDao() }

                var currentSeconds by rememberSaveable { mutableIntStateOf(0) }
                var refreshTrigger by remember { mutableIntStateOf(0) }

                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

                Scaffold(
                    modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        TopAppBar(
                            title = { Text(route?.name ?: "Szczegóły") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Wróć"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.shadow(4.dp),
                            scrollBehavior = scrollBehavior
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            if (route != null) {
                                val minutes = currentSeconds / 60
                                val seconds = currentSeconds % 60
                                val timeString = "%02d:%02d".format(minutes, seconds)

                                val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                                val currentDateTime = formatter.format(Date())

                                val record = RouteRecord(
                                    routeId = route.id,
                                    routeName = route.name,
                                    timeString = timeString,
                                    dateString = currentDateTime
                                )

                                coroutineScope.launch {
                                    dao.insertRecord(record)
                                    refreshTrigger++
                                    Toast.makeText(context, "Zapisano trasę!", Toast.LENGTH_LONG).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Wyślij")
                        }
                    }
                ) { innerPadding ->
                    RouteDetailContent(
                        route = route,
                        modifier = Modifier.padding(innerPadding),
                        seconds = currentSeconds,
                        onSecondsChange = { currentSeconds = it },
                        refreshTrigger = refreshTrigger,
                        onRecordSaved = { refreshTrigger++ }
                    )
                }
            }
        }
    }
}