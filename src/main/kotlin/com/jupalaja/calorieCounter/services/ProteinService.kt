package com.jupalaja.calorieCounter.services

import com.jupalaja.calorieCounter.domain.dto.NutritionResponseDTO
import com.jupalaja.calorieCounter.infra.output.adapters.calorieNinjas.CalorieNinjasAdapter
import org.springframework.stereotype.Service

@Service
class ProteinService(
    private val calorieNinjasAdapter: CalorieNinjasAdapter,
) {
    fun getTotalProtein(query: String): NutritionResponseDTO {
        if (query.isBlank()) {
            throw IllegalArgumentException("Query cannot be blank.")
        }
        return calorieNinjasAdapter.getNutritionInfo(query)
    }
}
