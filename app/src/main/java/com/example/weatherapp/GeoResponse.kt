package com.example.weatherapp

data class GeoResponse(
    val code: String,
    val location: List<GeoLocation>
)

data class GeoLocation(
    val name: String,
    val id: String,
    val lat: String,
    val lon: String,
    val adm2: String,
    val adm1: String,
    val country: String
)