package org.example.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Locale
import java.util.TimeZone

@Service
class GeoFilterService(
    private val geoBlacklistService: GeoBlacklistService
) {

    private val log = LoggerFactory.getLogger(GeoFilterService::class.java)

    fun detectNodeCountry(): String {
        //System.setProperty("geo.override.country", "IT")

        System.getProperty("geo.override.country")
            ?.uppercase()
            ?.takeIf { it.length == 2 }
            ?.also { log.debug("[GeoFilter] Tara din override de sistem: $it") }
            ?.let { return it }

        System.getProperty("user.country")
            ?.uppercase()
            ?.takeIf { it.length == 2 }
            ?.also { log.debug("[GeoFilter] Tara din user.country: $it") }
            ?.let { return it }

        Locale.getDefault().country
            .uppercase()
            .takeIf { it.length == 2 }
            ?.also { log.debug("[GeoFilter] Tara din Locale.getDefault(): $it") }
            ?.let { return it }

        val tzCountry = inferCountryFromTimezone(TimeZone.getDefault().id)
        if (tzCountry != null) {
            log.debug("[GeoFilter] Tara dedusa din TimeZone '${TimeZone.getDefault().id}': $tzCountry")
            return tzCountry
        }

        log.warn("[GeoFilter] Nu s-a putut determina tara nodului – se utilizeaza 'XX' (necunoscut).")
        return "XX"
    }

    fun detectNodeRegion(): String? {
        val locale = Locale.getDefault()
        val country = locale.country.uppercase().takeIf { it.length == 2 } ?: return null
        val region  = locale.variant.uppercase().takeIf { it.isNotBlank() } ?: return null
        return "$country/$region"
    }

    data class GeoCheckResult(
        val blocked: Boolean,
        val country: String,
        val region:  String?,
        val reason:  String?
    )

    fun checkCurrentNode(): GeoCheckResult {
        val country = detectNodeCountry()
        val region  = detectNodeRegion()

        // Verificare la nivel de țară
        if (country in geoBlacklistService.blacklistedCountries) {
            return GeoCheckResult(
                blocked = true,
                country = country,
                region  = region,
                reason  = "Tara '$country' se afla pe lista neagra. " +
                        "Accesul la aceste informatii nu este permis pentru zona geografica $country."
            )
        }

        if (region != null && region.uppercase() in geoBlacklistService.blacklistedRegions) {
            return GeoCheckResult(
                blocked = true,
                country = country,
                region  = region,
                reason  = "Regiunea '$region' se afla pe lista neagra. " +
                        "Accesul la aceste informatii nu este permis pentru zona geografica $region."
            )
        }

        return GeoCheckResult(blocked = false, country = country, region = region, reason = null)
    }

    private fun inferCountryFromTimezone(tzId: String): String? {
        val prefix = tzId.substringAfter("/", "").uppercase()
        return TIMEZONE_COUNTRY_MAP.entries
            .firstOrNull { tzId.startsWith(it.key, ignoreCase = true) }
            ?.value
    }

    companion object {
        private val TIMEZONE_COUNTRY_MAP = mapOf(
            "Europe/Bucharest"    to "RO",
            "Europe/London"       to "GB",
            "Europe/Paris"        to "FR",
            "Europe/Berlin"       to "DE",
            "Europe/Moscow"       to "RU",
            "Europe/Kiev"         to "UA",
            "Europe/Warsaw"       to "PL",
            "Europe/Budapest"     to "HU",
            "Europe/Sofia"        to "BG",
            "Europe/Belgrade"     to "RS",
            "Europe/Athens"       to "GR",
            "Europe/Rome"         to "IT",
            "Europe/Madrid"       to "ES",
            "Europe/Lisbon"       to "PT",
            "Europe/Amsterdam"    to "NL",
            "Europe/Brussels"     to "BE",
            "Europe/Stockholm"    to "SE",
            "Europe/Oslo"         to "NO",
            "Europe/Helsinki"     to "FI",
            "Europe/Copenhagen"   to "DK",
            "Europe/Zurich"       to "CH",
            "Europe/Vienna"       to "AT",
            "Europe/Prague"       to "CZ",
            "Europe/Bratislava"   to "SK",
            "Europe/Vilnius"      to "LT",
            "Europe/Riga"         to "LV",
            "Europe/Tallinn"      to "EE",
            "America/New_York"    to "US",
            "America/Chicago"     to "US",
            "America/Denver"      to "US",
            "America/Los_Angeles" to "US",
            "America/Toronto"     to "CA",
            "America/Vancouver"   to "CA",
            "America/Sao_Paulo"   to "BR",
            "America/Mexico_City" to "MX",
            "America/Buenos_Aires" to "AR",
            "Asia/Tokyo"          to "JP",
            "Asia/Shanghai"       to "CN",
            "Asia/Beijing"        to "CN",
            "Asia/Seoul"          to "KR",
            "Asia/Kolkata"        to "IN",
            "Asia/Dubai"          to "AE",
            "Asia/Singapore"      to "SG",
            "Asia/Bangkok"        to "TH",
            "Asia/Pyongyang"      to "KP",
            "Asia/Tehran"         to "IR",
            "Africa/Cairo"        to "EG",
            "Africa/Johannesburg" to "SA",
            "Australia/Sydney"    to "AU",
            "Pacific/Auckland"    to "NZ"
        )
    }
}