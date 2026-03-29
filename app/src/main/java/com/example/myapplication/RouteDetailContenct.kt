package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RouteDetailContent(route: Route?, modifier: Modifier = Modifier) {
    if (route == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Wybierz trasę z listy po lewej", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        Column(modifier = modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
            Text(text = route.name, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Typ: ${route.type}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Opis trasy:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = route.description, style = MaterialTheme.typography.bodyLarge)
            RouteStopwatch()
        }
    }
}