package org.example.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.services.GeoFilterService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class GeoFilterInterceptor(
    private val geoFilterService: GeoFilterService
) : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(GeoFilterInterceptor::class.java)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {

        val checkResult = geoFilterService.checkCurrentNode()

        if (checkResult.blocked) {
            val zone = checkResult.region ?: checkResult.country

            val message = buildString {
                append("ACCES INTERZIS (HTTP 403)\n\n")
                append("Accesul la aceste informatii nu este permis ")
                append("pentru zona geografica: $zone\n\n")
                append("Motivul: ${checkResult.reason}\n\n")
            }

            log.warn(
                "[GeoFilter] Request BLOCAT: {} {} | Nod: tara={}, regiune={} | Motiv: {}",
                request.method,
                request.requestURI,
                checkResult.country,
                checkResult.region ?: "N/A",
                checkResult.reason
            )

            response.status       = HttpStatus.FORBIDDEN.value()
            response.contentType  = "text/plain; charset=UTF-8"
            response.characterEncoding = "UTF-8"
            response.writer.write(message)
            return false
        }

        log.debug(
            "[GeoFilter] Request PERMIS: {} {} | Nod: tara={}, regiune={}",
            request.method,
            request.requestURI,
            checkResult.country,
            checkResult.region ?: "N/A"
        )

        return true
    }
}