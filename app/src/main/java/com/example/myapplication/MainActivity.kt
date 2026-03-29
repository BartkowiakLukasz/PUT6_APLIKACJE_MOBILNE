package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val myRoutes = listOf(
            Route(1, "Wokół Jeziora", "Przyjemna płaska trasa asfaltowa, idealna dla początkujących.", "Rowerowa"),
            Route(2, "Leśny Szlak", "Wymagająca trasa przez gęsty las z podbiegami korzeniowymi.", "Biegowa"),
            Route(3, "Górska Wspinaczka", "Trudna trasa z dużymi przewyższeniami i stromymi zjazdami.", "Rowerowa")
        )

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RouteList(
                        routes = myRoutes,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun RouteList(routes: List<Route>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(routes) { route ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable {
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