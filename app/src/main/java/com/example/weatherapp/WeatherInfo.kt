package com.example.weatherapp

data class WeatherResponse(
    val code: String,
    val updateTime: String,
    val now: Now
)

data class Now(
    val obsTime: String,
    val temp: String,
    val text: String
)