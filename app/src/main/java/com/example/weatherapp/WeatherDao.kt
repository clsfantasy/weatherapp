package com.example.weatherapp

import androidx.room.*

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather WHERE locationId = :locationId")
    fun getWeather(locationId: String): WeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWeather(weather: WeatherEntity)
}