package com.jupalaja.calorieCounter.infra.output.adapters.calorieNinjas

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class CalorieNinjasAdapter(
    @Value("\${api.calorieninjas.url}") apiUrl: String,
    @Value("\${api.calorieninjas.key}") apiKey: String,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val calorieNinjasWebClient: WebClient =
        WebClient
            .builder()
            .baseUrl(apiUrl)
            .defaultHeader("X-Api-Key", apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()

    fun getNutritionInfo(query: String): String =
        try {
            val response =
                this.calorieNinjasWebClient
                    .get()
                    .uri { uriBuilder ->
                        uriBuilder
                            .path("nutrition")
                            .queryParam("query", query)
                            .build()
                    }.retrieve()
                    .bodyToMono<String>()
                    .block()
            response?.takeIf { it.isNotBlank() } ?: throw IllegalStateException("API returned empty or blank body")
        } catch (e: Exception) {
            this.logger.error("[GET_NUTRITION_INFO] Error fetching nutrition info for query: {}", query, e)
            throw RuntimeException("Error fetching data from CalorieNinjas API for query: $query", e)
        }
}
