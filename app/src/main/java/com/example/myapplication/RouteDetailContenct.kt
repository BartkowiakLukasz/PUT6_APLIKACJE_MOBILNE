package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun RouteDetailContent(
    route: Route?, 
    modifier: Modifier = Modifier, 
    seconds: Int, 
    onSecondsChange: (Int) -> Unit,
    refreshTrigger: Int = 0,
    onRecordSaved: () -> Unit = {}
) {
    if (route == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Wybierz trasę z listy po lewej", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        Column(modifier = modifier.padding(24.dp).verticalScroll(rememberScrollState())) {

            AsyncImage(
                model = route.imageUrl,
                contentDescription = "Większe zdjęcie trasy",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = route.name, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Typ: ${route.type}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Opis trasy:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = route.description, style = MaterialTheme.typography.bodyLarge)

            RouteStopwatch(
                route = route,
                seconds = seconds,
                onSecondsChange = onSecondsChange,
                refreshTrigger = refreshTrigger,
                onRecordSaved = onRecordSaved
            )
        }
    }
}