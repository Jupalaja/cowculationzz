package com.jupalaja.calorieCounter.infra.output.ports

import com.jupalaja.calorieCounter.domain.dtos.calorieNinjas.NutritionResponseDTO

interface CalorieNinjasPort {
    fun getNutritionInfo(query: String): NutritionResponseDTO
}
