package com.example.weatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemWeatherHistoryBinding

sealed class HistoryItem {
    data class DateHeader(val date: String) : HistoryItem()
    data class Weather(val entity: WeatherEntity) : HistoryItem()
}

class WeatherHistoryAdapter(
    private var data: List<HistoryItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_WEATHER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is HistoryItem.DateHeader -> TYPE_HEADER
            is HistoryItem.Weather -> TYPE_WEATHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            HeaderViewHolder(view)
        } else {
            val binding = ItemWeatherHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            WeatherViewHolder(binding)
        }
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = data[position]) {
            is HistoryItem.DateHeader -> (holder as HeaderViewHolder).bind(item)
            is HistoryItem.Weather -> (holder as WeatherViewHolder).bind(item.entity)
        }
    }

    fun updateData(newData: List<HistoryItem>) {
        data = newData
        notifyDataSetChanged()
    }

    class HeaderViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        fun bind(item: HistoryItem.DateHeader) {
            (itemView as android.widget.TextView).text = item.date
        }
    }

    class WeatherViewHolder(val binding: ItemWeatherHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: WeatherEntity) {
            binding.tvCity.text = entity.cityName
            binding.tvTemp.text = "${entity.temp}℃ ${entity.text}"
            binding.tvDate.text = "更新时间: ${entity.updateTime}"
        }
    }
}