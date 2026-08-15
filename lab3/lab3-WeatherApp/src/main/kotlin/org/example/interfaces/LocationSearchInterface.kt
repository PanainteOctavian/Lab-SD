package org.example.interfaces

interface LocationSearchInterface {
    fun getLocationData(locationName: String): Pair<Double, Double>
}