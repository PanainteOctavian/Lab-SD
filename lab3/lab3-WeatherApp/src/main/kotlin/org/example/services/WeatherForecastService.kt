package org.example.services

import org.example.interfaces.WeatherForecastInterface
import org.example.pojo.WeatherForecastData
import org.json.JSONObject
import org.springframework.stereotype.Service
import java.net.URL
import kotlin.math.roundToInt

@Service
class WeatherForecastService(private val timeService: TimeService) : WeatherForecastInterface {

    override fun getForecastData(coords: Pair<Double, Double>): WeatherForecastData {
        val forecastDataURL = URL(
            "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=${coords.first}" +
                    "&longitude=${coords.second}" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature," +
                    "precipitation_probability,cloud_cover,visibility,wind_speed_10m,wind_direction_10m,weathercode" +
                    "&daily=temperature_2m_max,temperature_2m_min" +
                    "&timezone=auto"
        )

        val rawResponse: String = forecastDataURL.readText()
        val responseRootObject = JSONObject(rawResponse)
        val currentWeather = responseRootObject.getJSONObject("current")
        val dailyWeather = responseRootObject.getJSONObject("daily")

        val weatherCode = currentWeather.optInt("weathercode", 0)

        return WeatherForecastData(
            location            = "lat=${coords.first}, lon=${coords.second}",
            date                = timeService.getCurrentTime(),
            weatherState        = mapWeatherCode(weatherCode),
            weatherStateIconURL = mapWeatherCodeToIconURL(weatherCode),
            windDirection       = currentWeather.optInt("wind_direction_10m", 0).toString(),
            windSpeed           = currentWeather.optDouble("wind_speed_10m", 0.0).roundToInt(),
            minTemp             = dailyWeather.getJSONArray("temperature_2m_min").getDouble(0).roundToInt(),
            maxTemp             = dailyWeather.getJSONArray("temperature_2m_max").getDouble(0).roundToInt(),
            currentTemp         = currentWeather.optDouble("temperature_2m", 0.0).roundToInt(),
            humidity            = currentWeather.optInt("relative_humidity_2m", 0)
        )
    }

    private fun mapWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75 -> "Snow"
            80, 81, 82 -> "Rain showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown"
        }
    }

    private fun mapWeatherCodeToIconURL(code: Int): String {
        val icon = when (code) {
            0 -> "01d"
            1, 2 -> "02d"
            3 -> "04d"
            45, 48 -> "50d"
            51, 53, 55, 61, 63, 65 -> "10d"
            71, 73, 75 -> "13d"
            80, 81, 82 -> "09d"
            95, 96, 99 -> "11d"
            else -> "01d"
        }
        return "https://openweathermap.org/img/wn/$icon@2x.png"
    }
}