package org.example.interfaces

import org.example.pojo.WeatherForecastData

interface WeatherOrchestrationInterface {
    fun getWeather(locationName: String): WeatherForecastData
}