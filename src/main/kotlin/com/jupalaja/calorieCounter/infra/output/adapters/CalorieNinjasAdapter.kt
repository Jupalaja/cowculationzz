package com.jupalaja.calorieCounter.infra.output.adapters

import com.jupalaja.calorieCounter.domain.dto.calorieNinjas.NutritionResponseDTO
import com.jupalaja.calorieCounter.infra.output.ports.CalorieNinjasPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono


@Component
class CalorieNinjasAdapter(
    private val calorieNinjasWebClient: WebClient
) : CalorieNinjasPort {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun getNutritionInfo(query: String): NutritionResponseDTO {
        logger.info("[GET_NUTRITION_INFO] Getting nutrition info for query: {}", query)
        return try {
            calorieNinjasWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("nutrition")
                        .queryParam("query", query)
                        .build()
                }
                .retrieve()
                .bodyToMono<NutritionResponseDTO>()
                .block() ?: throw IllegalStateException("API returned empty body")
        } catch (e: Exception) {
            logger.error("[GET_NUTRITION_INFO] Error getting nutrition info for query: {}", query, e)
            throw RuntimeException("Error fetching data from CalorieNinjas API for query: $query", e)
        }
    }
}
