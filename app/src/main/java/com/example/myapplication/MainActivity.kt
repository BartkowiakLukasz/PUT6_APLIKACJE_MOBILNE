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
import androidx.compose.runtime.getValue // Brakujący import dla 'by'
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue // Brakujący import dla 'by'
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray // Brakujący import dla JSON
import java.net.URL // Brakujący import dla URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
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
                        RouteList(
                            routes = myRoutes,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RouteList(routes: List<Route>, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(routes) { route ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable {
                        val intent = Intent(context, DetailsActivity::class.java)
                        intent.putExtra("ROUTE_DATA", route)
                        context.startActivity(intent)
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = route.name, style = MaterialTheme.typography.titleLarge)
                    Text(text = "Typ: ${route.type}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}