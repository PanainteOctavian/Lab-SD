package org.example.services

import org.example.interfaces.LocationSearchInterface
import org.example.interfaces.WeatherForecastInterface
import org.example.interfaces.WeatherOrchestrationInterface
import org.example.pojo.WeatherForecastData
import org.springframework.stereotype.Service

@Service
class WeatherOrchestrationService(
    private val locationSearchService: LocationSearchInterface,
    private val weatherForecastService: WeatherForecastInterface
) : WeatherOrchestrationInterface {

    override fun getWeather(locationName: String): WeatherForecastData {
        val coords = locationSearchService.getLocationData(locationName)
        return weatherForecastService.getForecastData(coords)
    }
}