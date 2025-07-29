package com.jupalaja.calorieCounter.services

import com.jupalaja.calorieCounter.domain.dtos.calorieNinjas.NutritionResponseDTO
import com.jupalaja.calorieCounter.infra.output.ports.CalorieNinjasPort
import org.springframework.stereotype.Service


@Service
class NutritionService(
    private val calorieNinjasPort: CalorieNinjasPort
) {
    fun getNutrition(query: String): NutritionResponseDTO {
        if (query.isBlank()) {
            throw IllegalArgumentException("Query cannot be blank.")
        }
        return calorieNinjasPort.getNutritionInfo(query)
    }
}
