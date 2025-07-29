package com.jupalaja.calorieCounter.infra.output.ports

import com.jupalaja.calorieCounter.domain.dto.calorieNinjas.NutritionResponseDTO

interface CalorieNinjasPort {
    fun getNutritionInfo(query: String): NutritionResponseDTO
}
