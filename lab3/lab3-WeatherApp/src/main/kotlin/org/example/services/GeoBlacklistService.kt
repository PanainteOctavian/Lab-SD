package org.example.services

import jakarta.annotation.PostConstruct
import org.json.JSONArray
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.net.URL

@Service
class GeoBlacklistService {

    private val log = LoggerFactory.getLogger(GeoBlacklistService::class.java)

    @Value("\${geo.blacklist.source:geo_blacklist.json}")
    private lateinit var blacklistSource: String

    val blacklistedCountries: MutableSet<String> = mutableSetOf()

    val blacklistedRegions: MutableSet<String> = mutableSetOf()

    @PostConstruct
    fun loadBlacklist() {
        blacklistedCountries.clear()
        blacklistedRegions.clear()

        val rawJson: String = try {
            readSource(blacklistSource)
        } catch (e: Exception) {
            log.error("[GeoBlacklist] Nu s-a putut încarca blacklist-ul din '$blacklistSource': ${e.message}")
            return
        }

        try {
            val root = org.json.JSONObject(rawJson)

            root.optJSONArray("blacklistedCountries")?.let { arr ->
                for (i in 0 until arr.length()) {
                    blacklistedCountries.add(arr.getString(i).uppercase())
                }
            }

            root.optJSONArray("blacklistedRegions")?.let { arr ->
                for (i in 0 until arr.length()) {
                    blacklistedRegions.add(arr.getString(i).uppercase())
                }
            }

            log.info(
                "[GeoBlacklist] Blacklist incarcat din '$blacklistSource': " +
                        "${blacklistedCountries.size} tari, ${blacklistedRegions.size} regiuni blocate."
            )
        } catch (e: Exception) {
            log.error("[GeoBlacklist] Eroare la parsarea JSON-ului blacklist: ${e.message}")
        }
    }

    private fun readSource(source: String): String {
        return when {
            source.startsWith("http://") || source.startsWith("https://") -> {
                log.info("[GeoBlacklist] Citire blacklist de la URL: $source")
                URL(source).readText(Charsets.UTF_8)
            }
            else -> {
                val file = File(source).let {
                    if (it.isAbsolute) it else File(System.getProperty("user.dir"), source)
                }
                log.info("[GeoBlacklist] Citire blacklist din fișier: ${file.absolutePath}")
                file.readText(Charsets.UTF_8)
            }
        }
    }
}