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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow

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
                    try {
                        val downloadedRoutes = withContext(Dispatchers.IO) {
                            val connection = URL("https://api.npoint.io/39c7891eb1f75ac4bba0").openConnection() as java.net.HttpURLConnection
                            connection.connectTimeout = 5000
                            connection.readTimeout = 5000
                            val response = connection.inputStream.bufferedReader().use { it.readText() }
                            val jsonArray = JSONArray(response)
                            val list = mutableListOf<Route>()
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val id = item.getInt("id")
                                list.add(
                                    Route(
                                        id = id,
                                        name = item.getString("name"),
                                        description = item.getString("description"),
                                        type = item.getString("type"),
                                        imageUrl = item.optString("imageUrl", "https://picsum.photos/seed/$id/400/300")
                                    )
                                )
                            }
                            list
                        }
                        myRoutes = downloadedRoutes
                    } catch (e: Exception) {
                        errorMessage = "Nie udało się pobrać tras. Sprawdź połączenie z internetem!"
                    } finally {
                        isLoading = false
                    }
                }

                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text("Menu nawigacyjne", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                            HorizontalDivider()
                            NavigationDrawerItem(
                                label = { Text("O aplikacji") },
                                selected = false,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    android.widget.Toast.makeText(context, "Aplikacja Trasy Rowerowe v1.0", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                label = { Text("Odśwież trasy") },
                                selected = false,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    refreshTrigger++
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                ) {
                    Scaffold(
                    modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    if (isTablet && selectedRoute != null)
                                        selectedRoute?.name ?: "Szczegóły"
                                    else
                                        "Trasy Rowerowe"
                                )
                            },
                            navigationIcon = {
                                if (isTablet && selectedRoute != null) {
                                    IconButton(onClick = { selectedRoute = null }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                                    }
                                } else {
                                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu")
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
                                    MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.shadow(4.dp),
                            scrollBehavior = scrollBehavior,
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
                        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (errorMessage != null) {
                        ErrorView(errorMessage!!, onRetry = { refreshTrigger++ }, modifier = Modifier.padding(innerPadding))
                    } else {
                        if (isTablet) {
                            Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                                MainTabsScreen(
                                    routes = myRoutes,
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    onRouteSelected = { selectedRoute = it }
                                )
                                VerticalDivider(modifier = Modifier.width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                RouteDetailContent(
                                    route = selectedRoute,
                                    modifier = Modifier.weight(1.5f).fillMaxHeight(),
                                    seconds = tabletSeconds,
                                    onSecondsChange = { tabletSeconds = it },
                                    refreshTrigger = refreshTrigger,
                                    onRecordSaved = { refreshTrigger++ }
                                )
                            }
                        } else {
                            MainTabsScreen(
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
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Warning, "Błąd", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) { Text("Spróbuj ponownie") }
        }
    }
}

@Composable
fun RouteList(routes: List<Route>, modifier: Modifier = Modifier, onRouteSelected: (Route) -> Unit) {
    if (routes.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Brak tras w tej kategorii", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
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
}

@Composable
fun MainTabsScreen(routes: List<Route>, modifier: Modifier = Modifier, onRouteSelected: (Route) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf("Start", "Rekreacyjne", "Wyczynowe")

    Column(modifier = modifier) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(title) }
                )
            }
        }
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> StartTabContent()
                1 -> {
                    val rekreacyjne = remember(routes) {
                        routes.filter { 
                            it.type.contains("łatwa", ignoreCase = true) || 
                            it.type.contains("asfalt", ignoreCase = true) ||
                            it.name.contains("Parkowy", ignoreCase = true) ||
                            it.name.contains("Oceanu", ignoreCase = true)
                        }
                    }
                    RouteList(routes = rekreacyjne, modifier = Modifier.fillMaxSize(), onRouteSelected = onRouteSelected)
                }
                2 -> {
                    val wyczynowe = remember(routes) {
                        routes.filter { 
                            it.type.contains("trudna", ignoreCase = true) || 
                            it.type.contains("MTB", ignoreCase = true) || 
                            it.type.contains("góry", ignoreCase = true) ||
                            it.name.contains("Leśny", ignoreCase = true) ||
                            it.name.contains("Wspinaczka", ignoreCase = true)
                        }
                    }
                    RouteList(routes = wyczynowe, modifier = Modifier.fillMaxSize(), onRouteSelected = onRouteSelected)
                }
            }
        }
    }
}

@Composable
fun StartTabContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1511994298241-608e28f14fde?q=80&w=800&auto=format&fit=crop",
            contentDescription = "Tło",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.2f
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Witaj w Trasach Rowerowych!",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Twoja osobista aplikacja do odkrywania najlepszych ścieżek. Wybierz kategorię z paska powyżej, aby znaleźć trasę dopasowaną do Twoich umiejętności. Mierz swój czas na szlaku za pomocą wbudowanego stopera, bij własne rekordy i udostępniaj je znajomym!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}