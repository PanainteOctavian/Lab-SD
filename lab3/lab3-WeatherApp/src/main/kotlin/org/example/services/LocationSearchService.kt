package org.example.services

import org.example.interfaces.LocationSearchInterface
import org.springframework.stereotype.Service
import java.net.URL
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class LocationSearchService : LocationSearchInterface {

    override fun getLocationData(locationName: String): Pair<Double, Double> {
        val encodedLocationName = URLEncoder.encode(locationName,
            StandardCharsets.UTF_8.toString())

        val locationSearchURL = URL(
            "https://geocoding-api.open-meteo.com/v1/search" +
                    "?name=$encodedLocationName&count=2&language=en&format=json"
        )

        val rawResponse: String = locationSearchURL.readText()
        val responseRootObject = JSONObject(rawResponse)
        val firstResult = responseRootObject
            .optJSONArray("results")
            ?.optJSONObject(0)
            ?: throw IllegalArgumentException("No results found for location: $locationName")

        val lat = firstResult.getDouble("latitude")
        val lon = firstResult.getDouble("longitude")
        return Pair(lat, lon)
    }
}