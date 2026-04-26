@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            var isDarkMode by rememberSaveable { mutableStateOf(systemInDarkTheme) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                val configuration = LocalConfiguration.current
                val isTablet = configuration.screenWidthDp >= 600
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val dao = remember { AppDatabase.getDatabase(context).routeDao() }

                var refreshTrigger by remember { mutableIntStateOf(0)}

                var selectedRoute by rememberSaveable { mutableStateOf<Route?>(null) }
                var tabletSeconds by rememberSaveable { mutableIntStateOf(0) }
                var myRoutes by remember { mutableStateOf<List<Route>>(emptyList()) }

                var isLoading by remember { mutableStateOf(true) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                // Wspólna funkcja zapisu dla FAB (Wyślij)
                val saveRouteAction = {
                    selectedRoute?.let { route ->
                        val minutes = tabletSeconds / 60
                        val seconds = tabletSeconds % 60
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
                            android.widget.Toast.makeText(context, "Zapisano trasę!", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME && errorMessage != null) {
                            refreshTrigger++
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                LaunchedEffect(refreshTrigger) {
                    isLoading = true
                    errorMessage = null
                    withContext(Dispatchers.IO) {
                        try {
                            val response = URL("https://api.npoint.io/39c7891eb1f75ac4bba0").readText()
                            val jsonArray = JSONArray(response)
                            val downloadedRoutes = mutableListOf<Route>()
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val id = item.getInt("id")
                                downloadedRoutes.add(
                                    Route(
                                        id = id,
                                        name = item.getString("name"),
                                        description = item.getString("description"),
                                        type = item.getString("type"),
                                        imageUrl = item.optString("imageUrl", "https://picsum.photos/seed/$id/400/300")
                                    )
                                )
                            }
                            myRoutes = downloadedRoutes
                            isLoading = false
                        } catch (e: Exception) {
                            errorMessage = "Nie udało się pobrać tras. Sprawdź połączenie z internetem!"
                            isLoading = false
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    if (isTablet && selectedRoute != null)
                                        selectedRoute?.name ?: "Szczegóły"
                                    else
                                        "Trasy"
                                )
                            },
                            navigationIcon = {
                                if (isTablet && selectedRoute != null) {
                                    IconButton(onClick = { selectedRoute = null }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Wróć"
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = if (isTablet && selectedRoute != null)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = if (isTablet && selectedRoute != null)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onPrimaryContainer,
                                navigationIconContentColor = if (isTablet && selectedRoute != null)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onPrimaryContainer,
                                actionIconContentColor = if (isTablet && selectedRoute != null)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.shadow(4.dp),
                            actions = {
                                IconButton(onClick = { isDarkMode = !isDarkMode }) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Przełącz tryb"
                                    )
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        if (isTablet && selectedRoute != null) {
                            FloatingActionButton(onClick = { saveRouteAction() }) {
                                Icon(Icons.Default.Send, contentDescription = "Wyślij")
                            }
                        }
                    }
                ) { innerPadding ->
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else if (errorMessage != null) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Błąd",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = { refreshTrigger++ }) {
                                    Text("Spróbuj ponownie")
                                }
                            }
                        }
                    }
                    else {
                        if (isTablet) {
                            Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                                RouteList(
                                    routes = myRoutes,
                                    modifier = Modifier.weight(1f),
                                    onRouteSelected = { selectedRoute = it }
                                )
                                VerticalDivider(modifier = Modifier.width(1.dp))
                                RouteDetailContent(
                                    route = selectedRoute,
                                    modifier = Modifier.weight(1.5f),
                                    seconds = tabletSeconds,
                                    onSecondsChange = { tabletSeconds = it },
                                    refreshTrigger = refreshTrigger,
                                    onRecordSaved = { refreshTrigger++ }
                                )
                            }
                        } else {
                            RouteList(
                                routes = myRoutes,
                                modifier = Modifier.padding(innerPadding),
                                onRouteSelected = { route ->
                                    val intent = Intent(context, DetailsActivity::class.java)
                                    intent.putExtra("ROUTE_DATA", route)
                                    intent.putExtra("DARK_MODE", isDarkMode)
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RouteList(routes: List<Route>, modifier: Modifier = Modifier, onRouteSelected: (Route) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(routes) { route ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onRouteSelected(route) },
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {
                    AsyncImage(
                        model = route.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = route.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        modifier = Modifier.padding(12.dp).fillMaxWidth().height(48.dp)
                    )
                }
            }
        }
    }
}