package com.jupalaja.calorieCounter.services

import com.jupalaja.calorieCounter.infra.output.adapters.calorieNinjas.CalorieNinjasAdapter
import org.springframework.stereotype.Service

@Service
class ProteinService(
    private val calorieNinjasAdapter: CalorieNinjasAdapter,
) {
    fun getTotalProtein(query: String): String {
        if (query.isBlank()) {
            throw IllegalArgumentException("Query cannot be blank.")
        }
        return calorieNinjasAdapter.getNutritionInfo(query)
    }
}
