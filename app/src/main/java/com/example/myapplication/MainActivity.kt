package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val configuration = LocalConfiguration.current
                val isTablet = configuration.screenWidthDp >= 600
                val context = LocalContext.current

                var selectedRoute by rememberSaveable { mutableStateOf<Route?>(null) }
                var tabletSeconds by rememberSaveable { mutableIntStateOf(0) }
                var myRoutes by remember { mutableStateOf<List<Route>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
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
                            isLoading = false
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize().statusBarsPadding(), // Dodano padding dla paska statusu zamiast TopBar
                    floatingActionButton = {
                        if (isTablet && selectedRoute != null) {
                            FloatingActionButton(onClick = {
                                val minutes = tabletSeconds / 60
                                val seconds = tabletSeconds % 60
                                val timeText = "Mój czas na trasie ${selectedRoute?.name}: %02d:%02d".format(minutes, seconds)
                                android.widget.Toast.makeText(context, timeText, android.widget.Toast.LENGTH_LONG).show()
                            }) {
                                Icon(Icons.Default.Send, contentDescription = "Wyślij")
                            }
                        }
                    }
                ) { innerPadding ->
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
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
                                    onSecondsChange = { tabletSeconds = it }
                                )
                            }
                        } else {
                            RouteList(
                                routes = myRoutes,
                                modifier = Modifier.padding(innerPadding),
                                onRouteSelected = { route ->
                                    val intent = Intent(context, DetailsActivity::class.java)
                                    intent.putExtra("ROUTE_DATA", route)
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