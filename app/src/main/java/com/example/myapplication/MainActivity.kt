package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.saveable.rememberSaveable
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val configuration = LocalConfiguration.current
                val isTablet = configuration.screenWidthDp >= 600

                var selectedRoute by rememberSaveable { mutableStateOf<Route?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

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
                                    downloadedRoutes.add(
                                        Route(
                                            id = item.getInt("id"),
                                            name = item.getString("name"),
                                            description = item.getString("description"),
                                            type = item.getString("type")
                                        )
                                    )
                                }
                                myRoutes = downloadedRoutes
                                isLoading = false

                            } catch (e: Exception) {
                                e.printStackTrace()
                                isLoading = false
                            }
                        }
                    }
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
                                Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
                                RouteDetailContent(
                                    route = selectedRoute,
                                    modifier = Modifier.weight(1.5f)
                                )
                            }
                        } else {
                            val context = LocalContext.current
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

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(routes) { route ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable {
                        onRouteSelected(route)
                    }
            ) {

                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    val icon = if (route.type == "Rowerowa") {
                        Icons.Default.DirectionsBike
                    } else {
                        Icons.Default.DirectionsRun
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = "Ikona typu trasy",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(text = route.name, style = MaterialTheme.typography.titleLarge)
                        Text(text = "Typ: ${route.type}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}