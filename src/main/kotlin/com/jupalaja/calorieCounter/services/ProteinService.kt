package com.jupalaja.calorieCounter.services

import com.jupalaja.calorieCounter.infra.output.ports.CalorieNinjasPort
import org.springframework.stereotype.Service


@Service
class ProteinService(
    private val calorieNinjasPort: CalorieNinjasPort
) {
    fun getTotalProtein(query: String): Double {
        if (query.isBlank()) {
            throw IllegalArgumentException("Query cannot be blank.")
        }
        val nutritionResponse = calorieNinjasPort.getNutritionInfo(query)
        return nutritionResponse.items.sumOf { it.proteinG }
    }
}
