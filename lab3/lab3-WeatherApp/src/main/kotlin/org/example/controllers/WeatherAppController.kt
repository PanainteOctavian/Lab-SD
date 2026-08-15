package org.example.controllers

import org.example.interfaces.WeatherOrchestrationInterface
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class WeatherAppController {

    @Autowired
    private lateinit var weatherOrchestrationService: WeatherOrchestrationInterface

    @RequestMapping("/getforecast/{location}", method = [RequestMethod.GET])

    @ResponseBody
    fun getForecast(@PathVariable location: String): String {
        val weatherData = try {
            weatherOrchestrationService.getWeather(location)
        } catch (e: IllegalArgumentException) {
            return "Nu s-au putut gasi date meteo pentru cuvintele cheie \"$location\"!"
        }
        return weatherData.toString()
    }
}