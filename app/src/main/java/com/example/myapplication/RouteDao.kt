package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Insert
    suspend fun insertRecord(record: RouteRecord)

    // Pobiera czasy tylko dla konkretnej trasy (po jej ID), od najnowszych
    // Zmieniamy na Flow, aby Room automatycznie powiadamiał o zmianach w tabeli
    @Query("SELECT * FROM route_records WHERE routeId = :routeId ORDER BY id DESC LIMIT 10")
    fun getRecordsForRouteFlow(routeId: Int): Flow<List<RouteRecord>>

    @Query("SELECT * FROM route_records WHERE routeId = :routeId ORDER BY id DESC LIMIT 10")
    suspend fun getRecordsForRoute(routeId: Int): List<RouteRecord>
}