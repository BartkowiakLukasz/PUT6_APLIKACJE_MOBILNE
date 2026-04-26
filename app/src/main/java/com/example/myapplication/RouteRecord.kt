package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_records")
data class RouteRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routeId: Int,
    val routeName: String,
    val timeString: String,
    val dateString: String
)