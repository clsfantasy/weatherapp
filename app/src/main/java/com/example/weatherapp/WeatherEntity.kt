package com.example.weatherapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey val locationId: String,
    val cityName: String,
    val temp: String,
    val text: String,
    val updateTime: String, // 天气数据更新时间
    val cacheTime: Long     // 本地缓存时间（System.currentTimeMillis()）
)