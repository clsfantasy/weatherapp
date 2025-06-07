package com.example.weatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemWeatherHistoryBinding

class WeatherHistoryAdapter(
    private var data: List<WeatherEntity>
) : RecyclerView.Adapter<WeatherHistoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemWeatherHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWeatherHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.binding.tvCity.text = item.cityName
        holder.binding.tvTemp.text = "${item.temp}℃ ${item.text}"
        holder.binding.tvDate.text = "更新时间: ${item.updateTime}"
    }

    fun updateData(newData: List<WeatherEntity>) {
        data = newData
        notifyDataSetChanged()
    }
}