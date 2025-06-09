package com.example.weatherapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var weatherDao: WeatherDao
    private lateinit var historyAdapter: WeatherHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "weather-db"
        ).build()
        weatherDao = db.weatherDao()

        val etCity = findViewById<AutoCompleteTextView>(R.id.et_city)
        val btnSearch = findViewById<Button>(R.id.btn_search)
        val tvWeather = findViewById<TextView>(R.id.tv_weather)
        val rvHistory = findViewById<RecyclerView>(R.id.rv_history)
        historyAdapter = WeatherHistoryAdapter(listOf())
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter

        // 查询并展示历史，并设置自动提示
        lifecycleScope.launch {
            val allHistory = withContext(Dispatchers.IO) {
                weatherDao.getAllWeather()
            }
            historyAdapter.updateData(groupHistoryByDate(allHistory))
            // 获取标准城市名去重，限制最多5个
            val cityNames = allHistory.map { it.cityName }.distinct().take(5)
            val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, cityNames)
            etCity.setAdapter(adapter)
            etCity.threshold = 0 // 输入1个字母就提示
        }

        etCity.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etCity.showDropDown()
            }
        }
        etCity.setOnClickListener {
            etCity.showDropDown()
        }

        btnSearch.setOnClickListener {
            val cityName = etCity.text.toString().trim()
            if (cityName.isEmpty()) {
                tvWeather.text = "请输入城市名"
                return@setOnClickListener
            }
            if (!NetUtil.isNetworkAvailable(this)) {
                tvWeather.text = "无网络连接，请检查网络"
                return@setOnClickListener
            }
            lifecycleScope.launch {
                tvWeather.text = "查询中..."
                val locationResult = withContext(Dispatchers.IO) {
                    WeatherApi.getLocationId(cityName)
                }
                if (locationResult == null) {
                    tvWeather.text = "未找到该城市"
                    return@launch
                }
                val locationId = locationResult.id
                val standardCityName = locationResult.name
                val cache = withContext(Dispatchers.IO) {
                    weatherDao.getWeather(locationId)
                }
                val now = System.currentTimeMillis()
                if (cache != null && now - cache.cacheTime < 60 * 60 * 1000) {
                    tvWeather.text = "${cache.cityName}\n${cache.temp}℃ ${cache.text}\n更新时间:${cache.updateTime}\n(本地缓存)"
                    if (!NetUtil.isNetworkAvailable(this@MainActivity))
                        return@launch
                } else {
                    var weather: WeatherResponse? = null
                    var retry = 3
                    var isTimeout = false
                    while (retry > 0) {
                        weather = withContext(Dispatchers.IO) {
                            try {
                                WeatherApi.getWeather(locationId)
                            } catch (e: java.net.SocketTimeoutException) {
                                isTimeout = true
                                null
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (weather != null && weather.code == "200") break
                        retry--
                    }
                    if (weather != null && weather.code == "200") {
                        tvWeather.text = "$standardCityName\n${weather.now.temp}℃ ${weather.now.text}\n更新时间:${weather.updateTime}\n(网络)"
                        val entity = WeatherEntity(
                            locationId = locationId,
                            cityName = standardCityName, // 用标准名
                            temp = weather.now.temp,
                            text = weather.now.text,
                            updateTime = weather.updateTime,
                            cacheTime = now
                        )
                        // 在插入天气数据后刷新历史和提示
                        withContext(Dispatchers.IO) {
                            weatherDao.insertWeather(entity)
                            val allHistory = weatherDao.getAllWeather()
                            withContext(Dispatchers.Main) {
                                historyAdapter.updateData(groupHistoryByDate(allHistory))
                                val cityNames = allHistory.map { it.cityName }.distinct().take(5)
                                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, cityNames)
                                etCity.setAdapter(adapter)
                            }
                        }
                    } else {
                        tvWeather.text = if (isTimeout) "请求超时，请检查网络或稍后重试" else "获取天气失败，请检查网络或稍后重试"
                    }
                }
            }
        }
    }
}

fun groupHistoryByDate(history: List<WeatherEntity>): List<HistoryItem> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val grouped = history.groupBy { sdf.format(Date(it.cacheTime)) }
    val result = mutableListOf<HistoryItem>()
    for ((date, items) in grouped) {
        result.add(HistoryItem.DateHeader(date))
        result.addAll(items.map { HistoryItem.Weather(it) })
    }
    return result
}