package com.example.weatherapp

import java.net.URLEncoder
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import java.util.zip.GZIPInputStream

object WeatherApi {
    private val client = OkHttpClient()
    private val gson = Gson()

    
    private const val API_HOST = "p25u9v4gwy.re.qweatherapi.com"
    private const val API_KEY = "621d648610434d83804fc2c823f2d53c"

    fun getWeather(location: String): WeatherResponse? {
        val url = "https://$API_HOST/v7/weather/now?location=$location&key=$API_KEY"
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept-Encoding", "gzip")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body ?: return null
            val contentEncoding = response.header("Content-Encoding")
            val json = if (contentEncoding == "gzip") {
                GZIPInputStream(body.byteStream()).bufferedReader().use { it.readText() }
            } else {
                body.string()
            }
            return gson.fromJson(json, WeatherResponse::class.java)
        }
    }

    data class LocationResult(val id: String, val name: String)

    fun getLocationId(cityName: String): LocationResult? {
        val encodedCity = URLEncoder.encode(cityName, "UTF-8")
        val url = "https://$API_HOST/geo/v2/city/lookup?location=$encodedCity&key=$API_KEY"
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept-Encoding", "gzip")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body ?: return null
            val contentEncoding = response.header("Content-Encoding")
            val json = if (contentEncoding == "gzip") {
                GZIPInputStream(body.byteStream()).bufferedReader().use { it.readText() }
            } else {
                body.string()
            }
            val geoResponse = gson.fromJson(json, GeoResponse::class.java)
            val loc = geoResponse.location.firstOrNull() ?: return null
            return LocationResult(loc.id, loc.name)
        }
    }
}