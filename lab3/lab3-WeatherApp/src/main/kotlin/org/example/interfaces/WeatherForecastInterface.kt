package org.example.interfaces

import org.example.pojo.WeatherForecastData

interface WeatherForecastInterface {
    fun getForecastData(coords: Pair<Double, Double>): WeatherForecastData
}