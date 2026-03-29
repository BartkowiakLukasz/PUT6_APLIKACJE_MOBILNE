package com.example.myapplication
import java.io.Serializable

data class Route(
    val id: Int,
    val name: String,
    val description: String,
    val type: String
) : Serializable